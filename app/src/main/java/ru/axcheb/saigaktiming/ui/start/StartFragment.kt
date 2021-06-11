package ru.axcheb.saigaktiming.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.artemchep.bindin.bindIn
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.databinding.StartFragmentBinding
import ru.axcheb.saigaktiming.ui.finish.FinishActivity

class StartFragment : Fragment() {

    private val TAG = this::class.qualifiedName

    private val args: StartFragmentArgs by navArgs()

    private val viewModel: StartViewModel by viewModel {
        parametersOf(
            args.eventId,
            args.memberId
        )
    }

    private fun startActiveListener(item: ResultItem) {
        viewModel.handleStartActive(item.id)
    }

    private val adapter: StartAdapter by inject { parametersOf(args.eventId, args.memberId, ::startActiveListener) }

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: StartFragmentBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StartFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.startRecycler.adapter = adapter

        binding.startRecycler.setHasFixedSize(true)
        setListeners()
        observeData()
        return binding.root
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.startBtn.setOnClickListener {
            FinishActivity.start(args.eventId, args.memberId, 0, binding.root.context)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.bindIn(viewModel.resultItems) { adapter.submitList(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}