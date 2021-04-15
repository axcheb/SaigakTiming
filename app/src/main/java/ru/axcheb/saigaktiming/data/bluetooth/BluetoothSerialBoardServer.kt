package ru.axcheb.saigaktiming.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

private const val TAG = "BluetoothSerialBoardServer"

private const val DEVICE_NAME = "HC-06"

/**
 * https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createRfcommSocketToServiceRecord%28java.util.UUID%29
 * If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID:
 */
private const val SPP_UUID_SERIAL_BOARD = "00001101-0000-1000-8000-00805f9b34fb"


/**
 * Синглтон для работы с последовательным модулем bluetooth (Bluetooth serial board).
 * В данном случае код заточен на работу с модулем HC-06 @see [DEVICE_NAME],
 * т.к работа с другими модулями не предполагается.
 */
object BluetoothSerialBoardServer {

    // TODO проверить что будет, если сединение отвалится!!! Сделать реконнект.  bluetoothSocketFlow.value. .isConnected()

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // Нужно ли спросить включить блютус.
    private val _requestEnableBluetooth = MutableStateFlow(true)
    val requestEnableBluetooth: StateFlow<Boolean> = _requestEnableBluetooth

    private val bluetoothSocketFlow = MutableStateFlow<BluetoothSocket?>(null)

    val inMessageFlow = bluetoothSocketFlow.map { it?.inputStream }.filter { it != null }
        .flatMapLatest { inStream ->
            BufferedReader(InputStreamReader(inStream)).lineSequence().asFlow()
        }.catch { e ->
            Log.d(TAG, "Cant read message. ${e.message}")
            onError()
        }.flowOn(Dispatchers.IO)

    fun startServer() {
        _requestEnableBluetooth.value = !adapter.isEnabled
        //TODO проверять не работает ли уже сервер
        if (adapter.isEnabled) setupServer()
    }

    suspend fun send(msg: String, times: Int) {
        repeat(times) {
            send("%03d $msg".format(it))
        }
    }

    suspend fun send(msg: String) {
        if (msg.isEmpty()) {
            return
        }
        val outputStream = bluetoothSocketFlow.value?.outputStream ?: return
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.d(TAG, "Message send failed...\n ${throwable.message}")
            onError()
        }

        withContext(Dispatchers.IO + exceptionHandler) {
            @Suppress("BlockingMethodInNonBlockingContext")
            outputStream.write("$msg\n".toByteArray())
        }
    }

    fun close() {
        val btSocket = bluetoothSocketFlow.value
        btSocket?.inputStream?.close()
        btSocket?.outputStream?.close()
        btSocket?.close()
        bluetoothSocketFlow.value = null
    }

    private fun setupServer() {

        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            // TODO может сделать toast, что не получилось законектиться
            Log.d(TAG, "Connection to $DEVICE_NAME failed...\n ${throwable.message}")
            onError()
        }

        GlobalScope.launch(Dispatchers.IO + exceptionHandler) {
            val btDevice = adapter.bondedDevices.firstOrNull { it.name == DEVICE_NAME }
            if (btDevice != null) {
                val socket = connectToDevice(btDevice)
                onConnected(socket!!)
                Log.d(TAG, "Device connected to $DEVICE_NAME")
            } else {
                Log.d(TAG, "Connection failed.")
            }
        }
    }

    private suspend fun connectToDevice(btDevice: BluetoothDevice): BluetoothSocket? =
        GlobalScope.async(Dispatchers.IO) {
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
        if (bluetoothSocketFlow.value?.isConnected == false) {
            close()
            _requestEnableBluetooth.value = !adapter.isEnabled
        }
    }

}