package ru.axcheb.saigaktiming.ui.sensors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.axcheb.saigaktiming.databinding.SensorsFragmentBinding
import ru.axcheb.saigaktiming.ui.MainActivity

class SensorsFragment : Fragment() {

    private val viewModel: SensorsViewModel by viewModel()

    private var _binding: SensorsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SensorsFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        observeData()
        setListeners()
        return binding.root
    }

    private fun observeData() {
    }

    private fun setListeners() {
        val mainActivity = activity as MainActivity
        binding.syncTimeBtn.setOnClickListener {
            lifecycleScope.launch {
                mainActivity.btService.syncTime()
            }
        }

        binding.searchSensorsBtn.setOnClickListener {
            lifecycleScope.launch {
                mainActivity.btService.searchSensors()
            }
        }

        binding.clearBtn.setOnClickListener {
            viewModel.sensorsText.tryEmit("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}