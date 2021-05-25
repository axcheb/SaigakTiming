package ru.axcheb.saigaktiming.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.artemchep.bindin.bindIn
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService

class MainActivity : AppCompatActivity() {

    lateinit var btService: BluetoothSerialBoardService
    private var btServiceBound: Boolean = false

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startBtServer()
            }
        }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothSerialBoardService.LocalBinder
            btService = binder.getService()
            btServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            btServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startBtServer()

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        observeData()
    }

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothSerialBoardService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    private fun observeData() {
        this.bindIn(BluetoothSerialBoardService.requestEnableBluetooth) {
            if (it) {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun startBtServer() {
        Intent(this, BluetoothSerialBoardService::class.java).also { intent ->
            startService(intent)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}