package ru.axcheb.saigaktiming.ui.memberselect

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.databinding.NewMemberFragmentBinding

class NewMemberDialogFragment(eventId: Long) : DialogFragment() {

    private val TAG = this::class.qualifiedName

    private val viewModel: NewMemberViewModel by viewModel { parametersOf(eventId) }

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: NewMemberFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewMemberFragmentBinding.inflate(inflater, container, false)
        binding.vm = viewModel
        binding.lifecycleOwner = this
        retainInstance = true
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
        viewModel.addResult.observe(viewLifecycleOwner) {
            val result = it ?: return@observe
            if (result == AddResult.OK) {
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

}