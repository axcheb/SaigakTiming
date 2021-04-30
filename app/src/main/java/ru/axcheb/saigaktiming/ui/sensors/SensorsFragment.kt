package ru.axcheb.saigaktiming.ui.sensors

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.databinding.SensorsFragmentBinding
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService
import ru.axcheb.saigaktiming.ui.MainActivity
import java.lang.Integer.parseInt

class SensorsFragment : Fragment() {

    private lateinit var viewModel: SensorsViewModel

    private var _binding: SensorsFragmentBinding? = null
    private val binding get() = _binding!!

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startBtServer()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SensorsFragmentBinding.inflate(inflater, container, false)

        // TODO use koin to create VM
        viewModel =
            ViewModelProvider(this).get(SensorsViewModel::class.java)

        val textView = binding.textSensors
        viewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })


        observeData()
        setListeners()

        startBtServer()
        return binding.root
    }

    private var x = 0

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            BluetoothSerialBoardService.messageFlow.collect {
                if (x == 0) {
                    x = parseInt(it)
                }

                if (binding.textSensors.text.length > 1000) {
                    binding.textSensors.text = ""
                }
                binding.textSensors.text = it + " - " + x + "\n" + binding.textSensors.text
                x++
//                showToast(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            BluetoothSerialBoardService.requestEnableBluetooth.collect {
                if (it) {
                    enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }
        }
    }

    private fun setListeners() {
        val mainActivity = activity as MainActivity

        binding.sendMessageBtn.setOnClickListener {
            lifecycleScope.launch {
                mainActivity.btService.send(binding.messageInput.editText?.text.toString())
                binding.messageInput.editText?.text?.clear()
            }
        }

    }

    private fun startBtServer() {
//        activity?.startService(Intent(activity, BluetoothSerialBoardService::class.java))
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}