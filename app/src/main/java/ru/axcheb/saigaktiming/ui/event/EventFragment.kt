package ru.axcheb.saigaktiming.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.axcheb.saigaktiming.databinding.EventFragmentBinding
import ru.axcheb.saigaktiming.ui.finish.FinishActivity
import ru.axcheb.saigaktiming.ui.memberselect.MemberSelectActivity

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
            val eventId = viewModel.event.value?.id
            if (eventId != null) {
                context?.let { it1 -> MemberSelectActivity.start(eventId, it1) }
            }
        }

        binding.startAllLine.setOnClickListener {
            val eventId = viewModel.event.value?.id
            if (eventId != null) {
                FinishActivity.start(eventId, 0, 0, binding.root.context)
            }
        }

    }

    private fun observeData() {
        viewModel.members.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}