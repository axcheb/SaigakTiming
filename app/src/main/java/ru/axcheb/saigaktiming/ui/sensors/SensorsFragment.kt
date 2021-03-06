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
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SensorsFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setListeners()
    }

    private fun observeData() {
    }

    private fun setListeners() {
        val mainActivity = activity as MainActivity
        binding.syncTimeBtn.setOnClickListener {
            mainActivity.btService.syncTime()
        }

        binding.searchSensorsBtn.setOnClickListener {
            mainActivity.btService.searchSensors()
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