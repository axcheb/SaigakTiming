package ru.axcheb.saigaktiming.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.artemchep.bindin.bindIn
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.axcheb.saigaktiming.databinding.EventFragmentBinding
import ru.axcheb.saigaktiming.ui.finish.FinishActivity

class EventFragment : Fragment() {

    private val TAG = this::class.qualifiedName

    private val viewModel: EventViewModel by viewModel()

    private val adapter: EventMemberAdapter by inject()

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: EventFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EventFragmentBinding.inflate(inflater, container, false)
        binding.eventMemberRecycler.adapter = adapter
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        // TODO если захочется переделать на divider
//        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
//        divider.setDrawable(ContextCompat.getDrawable(activity, R.drawable.e_m_line_divider)
//        binding.eventMemberRecycler.addItemDecoration(divider)
        setListeners()
        observeData()
        return binding.root
    }

    private fun setListeners() {
        binding.addMemberLine.setOnClickListener {
            val eventId = viewModel.eventState.value?.id
            if (eventId != null) {
                val direction =
                    EventFragmentDirections.actionNavigationEventToNavigationMemberSelect(
                        eventId
                    )
                view?.findNavController()?.navigate(direction)
            }
        }

        binding.startAllLine.setOnClickListener {
            val eventId = viewModel.eventState.value?.id
            if (eventId != null) {
                FinishActivity.start(eventId, 0, 0, binding.root.context)
            }
        }

        binding.eventCard.setOnClickListener {
            navigateToEditEvent()
        }

        binding.competitionProtocolLine.setOnClickListener {
            val eventId = viewModel.eventState.value?.id
            if (eventId != null) {
                view?.findNavController()
                    ?.navigate(
                        EventFragmentDirections.actionNavigationEventToProtocolFragment(
                            eventId
                        )
                    )
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.bindIn(viewModel.eventShare) {
            if (it == null) {
                navigateToEditEvent()
            }
        }

        viewModel.members.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun navigateToEditEvent() {
        view?.findNavController()
            ?.navigate(EventFragmentDirections.actionNavigationEventToEditEventFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}