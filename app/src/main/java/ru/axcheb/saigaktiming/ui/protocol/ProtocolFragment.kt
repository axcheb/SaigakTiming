package ru.axcheb.saigaktiming.ui.protocol

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.axcheb.saigaktiming.databinding.ProtocolFragmentBinding

class ProtocolFragment : Fragment() {

    private val args: ProtocolFragmentArgs by navArgs()

    private val viewModel: ProtocolViewModel by viewModel {
        parametersOf(
            args.eventId
        )
    }

    private val adapter: ProtocolAdapter by inject()

    private var _binding: ProtocolFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProtocolFragmentBinding.inflate(inflater, container, false)
        binding.protocolRecycler.adapter = adapter
        observeData()
        return binding.root
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            viewModel.protocolItems.collect { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}