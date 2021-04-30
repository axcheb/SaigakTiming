package ru.axcheb.saigaktiming.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService.Companion.DEVICE_NAME
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*


/**
 * Синглтон для работы с последовательным модулем bluetooth (Bluetooth serial board).
 * В данном случае код заточен на работу с модулем HC-06 @see [DEVICE_NAME],
 * т.к работа с другими модулями не предполагается.
 */
class BluetoothSerialBoardService : LifecycleService() {

    companion object {
        // messages from bt adapter
        private val _messageFlow = MutableSharedFlow<String>()
        val messageFlow : Flow<String> = _messageFlow

        // Нужно ли спросить включить блютус:
        private val _requestEnableBluetooth = MutableStateFlow(true)
        val requestEnableBluetooth: StateFlow<Boolean> = _requestEnableBluetooth

        private const val RECONNECTION_TRY_TIME = 30_000L

        private const val DEVICE_NAME = "HC-06"

        /**
         * https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createRfcommSocketToServiceRecord%28java.util.UUID%29
         * If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID:
         */
        private const val SPP_UUID_SERIAL_BOARD = "00001101-0000-1000-8000-00805f9b34fb"

        private const val TAG = "BluetoothSerialBoardService"
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var status: ServiceStatus = ServiceStatus.STOPPED

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothSocketFlow = MutableStateFlow<BluetoothSocket?>(null)

    private val inMessageFlow = bluetoothSocketFlow.map { it?.inputStream }.filter { it != null }
        .flatMapLatest { inStream ->
            BufferedReader(InputStreamReader(inStream)).lineSequence().asFlow()
        }.catch { e ->
            Log.d(TAG, "Cant read message. ${e.message}")
            onError()
        }.flowOn(Dispatchers.IO)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_ON -> {
                            Log.d(TAG, "BluetoothAdapter.STATE_ON")
                            start()
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            close()
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "BluetoothDevice.BOND_BONDED")
                        start()
                    }
                }
            }
        }
    }

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothSerialBoardService = this@BluetoothSerialBoardService
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        Log.d(TAG, "onBind")
        start()
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

        registerReceiver(broadcastReceiver, filter)

        scope.launch {
            inMessageFlow.collect { str ->
                _messageFlow.emit(str)
            }
        }
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        close()
        scope.coroutineContext.cancelChildren()
        unregisterReceiver(broadcastReceiver)
    }

    private val tryToReconnectJob = scope.launch {
        while (true) {
            Log.d(TAG, "tryToReconnectJob...")
            delay(RECONNECTION_TRY_TIME)
            Log.d(TAG, "tryToReconnectJob start ...")
            start()
        }
    }

    /**
     * Ищет среди сопряженных устройств [DEVICE_NAME], затем подключается к нему.
     * Ничего не делает, если подключение уже существует.
     */
    private fun start() {
        scope.launch {
            withContext(Dispatchers.Main) {
                _requestEnableBluetooth.value = !adapter.isEnabled
                synchronized(status) {
                    val s = when (status) {
                        ServiceStatus.STOPPED -> "stopped"
                        ServiceStatus.STARTING -> "starting"
                        else -> "started"
                    }

                    Log.d(TAG, "Status = $s")
                    if (status == ServiceStatus.STOPPED && adapter.isEnabled && bluetoothSocketFlow.value == null) {
                        status = ServiceStatus.STARTING
                        setupService()
                    }
                }
            }
        }
    }

    suspend fun send(msg: String): Boolean {
        if (msg.isEmpty()) {
            return true
        }
        val outputStream = bluetoothSocketFlow.value?.outputStream ?: return false
        var res = true
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            res = false
            Log.d(TAG, "Message send failed...\n ${throwable.message}")
            onError()
        }

        withContext(Dispatchers.IO + exceptionHandler) {
            @Suppress("BlockingMethodInNonBlockingContext")
            outputStream.write("$msg\n".toByteArray())
        }
        return res
    }

    private fun close() {
        val btSocket = bluetoothSocketFlow.value
        btSocket?.inputStream?.close()
        btSocket?.outputStream?.close()
        btSocket?.close()
        bluetoothSocketFlow.value = null
        synchronized(status) {
            status = ServiceStatus.STOPPED
        }
    }

    private fun setupService() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "Connection to $DEVICE_NAME failed...\n ${throwable.message}")
            onError()
        }

        scope.launch(Dispatchers.IO + exceptionHandler) {
            val btDevice = adapter.bondedDevices.firstOrNull { it.name == DEVICE_NAME }
            if (btDevice != null) {
                val socket = connectToDevice(btDevice)
                onConnected(socket!!)
                synchronized(status) {
                    status = ServiceStatus.STARTED
                }
                Log.d(TAG, "Device connected to $DEVICE_NAME")
            } else {
                synchronized(status) {
                    status = ServiceStatus.STOPPED
                }
                Log.d(TAG, "Connection failed.")
            }
        }
    }

    private suspend fun connectToDevice(btDevice: BluetoothDevice): BluetoothSocket? =
        scope.async(Dispatchers.IO) {
            val bluetoothSocket = btDevice.createRfcommSocketToServiceRecord(
                UUID.fromString(
                    SPP_UUID_SERIAL_BOARD
                )
            )
            bluetoothSocket.connect()
            return@async bluetoothSocket
        }.await()

    private fun onConnected(socket: BluetoothSocket) {
        bluetoothSocketFlow.value = socket
    }

    private fun onError() {
        close()
    }
}

sealed class ServiceStatus {
    /** Сервис не общается с BT устройством. */
    object STOPPED : ServiceStatus()

    /** Идёт общение с BT устройством. */
    object STARTED : ServiceStatus()

    /** Сервис пытается соединиться с BT устройством. */
    object STARTING : ServiceStatus()
}