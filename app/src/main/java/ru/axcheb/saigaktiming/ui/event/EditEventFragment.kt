package ru.axcheb.saigaktiming.ui.event

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.databinding.EditEventFragmentBinding
import java.util.*

class EditEventFragment : Fragment() {

    private val viewModel: EditEventViewModel by viewModel()

    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: EditEventFragmentBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // do nothing for new event and return back for existing event
            if (viewModel.eventId.value != null) {
                view?.findNavController()?.navigateUp()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditEventFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setListeners()
        observeData()
        return binding.root
    }

    private fun setListeners() {
        binding.eventDate.setOnClickListener {
            openDatePicker()
        }
        binding.eventTime.setOnClickListener {
            openTimePicker()
        }
        binding.toolbar.setNavigationOnClickListener {
            if (viewModel.eventId.value != null) {
                view?.findNavController()?.navigateUp()
            }
        }
        binding.toolbar.setOnMenuItemClickListener {
            if (isAdded) {
                viewModel.save()
            }
            true
            // wait for saved state (viewModel.state) and then close
        }
    }

    private fun observeData() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state == EditEventViewModel.State.SAVED) {
                view?.findNavController()?.navigateUp()
            }
        }
    }

    private fun openDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setTitleText(R.string.start_date).also {
                val date = viewModel.eventDate.value
                if (date != null) it.setSelection(date.time)
            }
            .build()

        datePicker.addOnPositiveButtonClickListener {
            viewModel.setEventDate(Date(it))
        }
        datePicker.show(childFragmentManager, DATE_PICKER_DIALOG_TAG)
    }

    private fun openTimePicker() {
        val is24Hour = is24HourFormat(requireContext())
        val clockFormat = if (is24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val timePair = viewModel.getTime()
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(timePair.first)
            .setMinute(timePair.second)
            .setTitleText(R.string.start_time)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            viewModel.setEventTime(timePicker.hour, timePicker.minute)
        }

        timePicker.show(childFragmentManager, TIME_PICKER_DIALOG_TAG)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DATE_PICKER_DIALOG_TAG = "EVENT_DATE_PICKER_DIALOG"
        private const val TIME_PICKER_DIALOG_TAG = "EVENT_TIME_PICKER_DIALOG"
    }

}