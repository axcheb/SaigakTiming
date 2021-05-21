package ru.axcheb.saigaktiming.ui.protocol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.artemchep.bindin.bindIn
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
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
        viewLifecycleOwner.bindIn(viewModel.protocolItems) { adapter.submitList(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}