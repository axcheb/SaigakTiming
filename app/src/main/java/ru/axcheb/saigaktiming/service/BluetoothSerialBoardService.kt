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
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.axcheb.saigaktiming.data.SEARCH
import ru.axcheb.saigaktiming.data.TIME
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService.Companion.DEVICE_NAME
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Сервис для работы с последовательным модулем bluetooth (Bluetooth serial board).
 * В данном случае код заточен на работу с модулем HC-06 @see [DEVICE_NAME],
 * т.к работа с другими модулями не предполагается.
 */
class BluetoothSerialBoardService : LifecycleService() {

    companion object {
        // messages from bt adapter
        private val _messageFlow = MutableSharedFlow<String>()
        val messageFlow get() = _messageFlow.asSharedFlow()

        // Нужно ли спросить включить блютус:
        private val _requestEnableBluetooth = MutableStateFlow(true)
        val requestEnableBluetooth get() = _requestEnableBluetooth.asStateFlow()

        private const val RECONNECTION_TRY_TIME = 30_000L

        private const val DEVICE_NAME = "HC-06"

        /**
         * https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createRfcommSocketToServiceRecord%28java.util.UUID%29
         * If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID:
         */
        private const val SPP_UUID_SERIAL_BOARD = "00001101-0000-1000-8000-00805f9b34fb"

    }

    private val mutex = Mutex()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var status: ServiceStatus = ServiceStatus.STOPPED

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothSocketFlow = MutableStateFlow<BluetoothSocket?>(null)

    private val inMessageFlow = bluetoothSocketFlow.map { it?.inputStream }.filterNotNull()
        .flatMapLatest { inStream ->
            BufferedReader(InputStreamReader(inStream)).lineSequence().asFlow()
        }.catch { e ->
            Timber.d("Cant read message. ${e.message}")
            onError()
        }.flowOn(Dispatchers.IO)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )) {
                        BluetoothAdapter.STATE_ON -> {
                            Timber.d("BluetoothAdapter.STATE_ON")
                            start()
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            close()
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state =
                        intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothDevice.BOND_BONDED) {
                        Timber.d("BluetoothDevice.BOND_BONDED")
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

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        close()
        scope.coroutineContext.cancelChildren()
        unregisterReceiver(broadcastReceiver)
    }

    private val tryToReconnectJob = scope.launch {
        while (true) {
            delay(RECONNECTION_TRY_TIME)
            Timber.d("tryToReconnectJob start ...")
            start()
        }
    }

    /**
     * Ищет среди сопряженных устройств [DEVICE_NAME], затем подключается к нему.
     * Ничего не делает, если подключение уже существует.
     */
    private fun start() {
        if (adapter == null) {
            return
        }
        scope.launch {
            _requestEnableBluetooth.value = !adapter.isEnabled
            val mustStartService: Boolean
            mutex.withLock {
                mustStartService =
                    (status == ServiceStatus.STOPPED && adapter.isEnabled && bluetoothSocketFlow.value == null)
                if (mustStartService) {
                    status = ServiceStatus.STARTING
                }
            }
            if (mustStartService) {
                setupService()
            }
        }
    }

    fun send(msg: String): Boolean {
        if (msg.isEmpty()) {
            return true
        }
        val btSocket = bluetoothSocketFlow.value ?: return false
        if (!btSocket.isConnected) {
            return false
        }
        val outputStream = btSocket.outputStream ?: return false
        var res = true
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            res = false
            Timber.e("Message send failed...\n ${throwable.message}")
            onError()
        }
        scope.launch(Dispatchers.IO + exceptionHandler) {
            @Suppress("BlockingMethodInNonBlockingContext")
            outputStream.write("$msg\n".toByteArray())
        }
        return res
    }

    fun syncTime() {
        val d = Date()
        send("$TIME,${d.time / 1000}")
    }

    fun searchSensors() {
        send(SEARCH)
    }

    private fun close() {
        val btSocket = bluetoothSocketFlow.value
        try {
            btSocket?.inputStream?.close()
        } catch (e: IOException) {
            //do nothing
        }
        try {
            btSocket?.outputStream?.close()
        } catch (e: IOException) {
            //do nothing
        }
        try {
            btSocket?.close()
        } catch (e: IOException) {
            //do nothing
        }

        bluetoothSocketFlow.value = null
        status = ServiceStatus.STOPPED
    }

    private fun setupService() {
        if (adapter == null) {
            return
        }
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Timber.d("Connection to $DEVICE_NAME failed...\n ${throwable.message}")
            onError()
        }

        scope.launch(Dispatchers.IO + exceptionHandler) {
            val btDevice = adapter.bondedDevices.firstOrNull { it.name == DEVICE_NAME }
            if (btDevice != null) {
                val socket = connectToDevice(btDevice)
                onConnected(socket!!)
                mutex.withLock {
                    status = ServiceStatus.STARTED
                }
                Timber.d("Device connected to $DEVICE_NAME")
            } else {
                mutex.withLock {
                    status = ServiceStatus.STOPPED
                }
                Timber.d("Connection failed.")
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