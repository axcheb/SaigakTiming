package ru.axcheb.saigaktiming.ui.memberselect

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.databinding.NewMemberFragmentBinding

class NewMemberDialogFragment : DialogFragment() {

    private val TAG = this::class.qualifiedName

    private val viewModel: NewMemberViewModel by viewModel {
        parametersOf(
            requireArguments().getLong(EVENT_ID_EXTRA)
        )
    }

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: NewMemberFragmentBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewMemberFragmentBinding.inflate(inflater, container, false)
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setListeners()
        observeData()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.nameInput.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    viewModel.addNewMember()
                }
            }
            true
        }

        binding.addMemberBtn.setOnClickListener {
            viewModel.addNewMember()
        }
    }

    private fun observeData() {
        viewModel.state.observe(viewLifecycleOwner) {
            val result = it ?: return@observe
            if (result == NewMemberViewModel.State.SAVED) {
                val toast = Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val EVENT_ID_EXTRA = "EVENT_ID"
        private const val NEW_MEMBER_DIALOG_TAG = "NEW_MEMBER_DIALOG"

        fun showDialog(fragmentManager: FragmentManager, eventId: Long) {
            val args = Bundle()
            args.putLong(EVENT_ID_EXTRA, eventId)
            val newMemberDialog = NewMemberDialogFragment()
            newMemberDialog.arguments = args
            newMemberDialog.show(fragmentManager, NEW_MEMBER_DIALOG_TAG)
        }
    }

}