package ru.axcheb.saigaktiming.ui.memberselect

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
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem
import ru.axcheb.saigaktiming.databinding.MemberSelectFragmentBinding

class MemberSelectFragment : Fragment() {

    private val TAG = this::class.qualifiedName

    private val args: MemberSelectFragmentArgs by navArgs()

    private val viewModel: MemberSelectViewModel by viewModel {
        parametersOf(
            args.eventId
        )
    }

    private fun bindMemberListener(item: MemberSelectItem) {
        viewModel.handleBind(item.memberId)
    }

    private val adapter: MemberSelectAdapter by inject { parametersOf(::bindMemberListener) }

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: MemberSelectFragmentBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = MemberSelectFragmentBinding.inflate(inflater, container, false)

        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.allMembersItemRecycler.adapter = adapter
        setListeners()
        observeData()
        return binding.root
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener {
            if (isAdded) {
                NewMemberDialogFragment.showDialog(parentFragmentManager, viewModel.eventId)
            }
            true
        }
    }

    private fun observeData() {
        viewLifecycleOwner.bindIn(viewModel.members) { adapter.submitList(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}