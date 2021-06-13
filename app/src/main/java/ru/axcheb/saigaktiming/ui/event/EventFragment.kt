package ru.axcheb.saigaktiming.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.artemchep.bindin.bindIn
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.databinding.EventFragmentBinding
import ru.axcheb.saigaktiming.ui.addDivider
import ru.axcheb.saigaktiming.ui.finish.FinishActivity

class EventFragment : Fragment() {

    private val viewModel: EventViewModel by viewModel()

    private val adapter: EventMemberAdapter by inject()

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: EventFragmentBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EventFragmentBinding.inflate(inflater, container, false)
        binding.eventMemberRecycler.adapter = adapter
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.eventMemberRecycler.addDivider(R.drawable.member_divider)
        setListeners()
        observeData()
        return binding.root
    }

    private fun setListeners() {
        binding.addMember.setOnClickListener {
            val eventId = viewModel.eventState.value?.id
            if (eventId != null) {
                val direction =
                    EventFragmentDirections.actionNavigationEventToNavigationMemberSelect(
                        eventId
                    )
                view?.findNavController()?.navigate(direction)
            }
        }

        binding.startAll.setOnClickListener {
            val eventId = viewModel.eventState.value?.id
            if (eventId != null) {
                FinishActivity.start(eventId, 0, 0, binding.root.context)
            }
        }

        binding.eventCard.setOnClickListener {
            navigateToEditEvent()
        }

        binding.toArchive.setOnClickListener {
            val c = context
            if (c != null) {
                AlertDialog.Builder(c)
                    .setTitle(R.string.are_you_sure)
                    .setNegativeButton(R.string.cancel) { _, _ -> /* do nothing */ }
                    .setPositiveButton(R.string.to_archive) { _, _ ->  viewModel.toArchive() }
                    .show()
            }
        }

        binding.competitionProtocol.setOnClickListener {
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

        viewLifecycleOwner.bindIn(viewModel.members) {
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