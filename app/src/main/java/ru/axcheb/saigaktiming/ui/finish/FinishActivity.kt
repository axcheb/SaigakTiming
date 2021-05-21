package ru.axcheb.saigaktiming.ui.finish

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.artemchep.bindin.bindIn
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.databinding.FinishActivityBinding
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService

class FinishActivity : AppCompatActivity() {

    private val TAG = this::class.qualifiedName

    private val viewModel: FinishViewModel by viewModel {
        parametersOf(
            intent.getLongExtra(EVENT_ID_EXTRA, 0),
            intent.getLongExtra(MEMBER_ID_EXTRA, 0),
            intent.getLongExtra(START_ID_EXTRA, 0)
        )
    }

    private fun finishActiveListener(item: ResultItem) {
        viewModel.handleFinishActive(item)
    }

    private val adapter: FinishAdapter by inject { parametersOf(::finishActiveListener) }

    private var _binding: FinishActivityBinding? = null
    private val binding get() = _binding!!

    lateinit var btService: BluetoothSerialBoardService
    private var btServiceBound: Boolean = false

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

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothSerialBoardService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FinishActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.finishRecycler.adapter = adapter

        // не вызывает requestLayout, все вьюхи одного размера
        binding.finishRecycler.setHasFixedSize(true)

        observeData()
        setListeners()
    }

    private fun setListeners() {
        val view = binding.root
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                        viewModel.newFinish()
                        return@setOnKeyListener true
                    }
                }
            }
            return@setOnKeyListener false
        }

        binding.nextLine.setOnClickListener {
            viewModel.onNext()
        }

        binding.pauseLine.setOnClickListener {
            viewModel.onPause()
        }

        binding.toEndLine.setOnClickListener {
            viewModel.toEnd()
        }

        binding.resumeLine.setOnClickListener {
            viewModel.onResume()
        }

    }

    private fun observeData() {
        this.bindIn(viewModel.finishItems) { items -> adapter.submitList(items) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val EVENT_ID_EXTRA = "EVENT_ID"
        private const val MEMBER_ID_EXTRA = "MEMBER_ID"
        private const val START_ID_EXTRA = "START_ID"

        fun start(eventId: Long, memberId: Long, startId: Long, context: Context) {
            val intent = Intent(context, FinishActivity::class.java)
            intent.putExtra(EVENT_ID_EXTRA, eventId)
            intent.putExtra(MEMBER_ID_EXTRA, memberId)
            intent.putExtra(START_ID_EXTRA, startId)
            context.startActivity(intent)
        }

    }

}