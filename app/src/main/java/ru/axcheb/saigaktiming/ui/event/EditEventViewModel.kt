package ru.axcheb.saigaktiming.ui.event

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.domain.Event
import ru.axcheb.saigaktiming.data.repository.EventRepository
import java.text.SimpleDateFormat
import java.util.*

class EditEventViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _eventId = MutableLiveData<Long?>()
    val eventId: LiveData<Long?> = _eventId
    val eventDate = MutableLiveData<Date>()
    val dateStr = eventDate.map { if (it == null) null else dateFormat.format(it) }
    val timeStr = eventDate.map { if (it == null) null else timeFormat.format(it) }
    val trackCount = MutableLiveData<Int>(1)
    val trackMaxTime = MutableLiveData<Int>(2)

    val trackCountError = trackCount.map { validateTrackCount(it) }
    val trackMaxTimeError = trackMaxTime.map { validateTrackMaxTime(it) }

    private val _state = MutableLiveData(State.EDITING)
    val state: LiveData<State> = _state

    val canSave = combine(
        trackCountError.asFlow(),
        trackMaxTimeError.asFlow(),
        state.asFlow()
    ) { trackCountErrorVal, trackMaxTimeErrorVal, stateVal ->
        trackCountErrorVal == null && trackMaxTimeErrorVal == null && stateVal == State.EDITING
    }.asLiveData()

    init {
        viewModelScope.launch {
            val event = eventRepository.getCurrentEvent().firstOrNull() ?: newEvent()
            _eventId.value = event.id
            eventDate.value = event.date
            trackCount.value = event.trackCount
            trackMaxTime.value = event.trackMaxTime
        }
    }

    fun save() {
        if (canSave.value == true) {
            viewModelScope.launch {
                val event = Event(
                    id = eventId.value,
                    date = eventDate.value!!,
                    trackCount = trackCount.value!!,
                    trackMaxTime = trackMaxTime.value!!,
                    inArchive = false
                )
                _state.value = State.SAVING
                if (event.id == null) {
                    eventRepository.insert(event)
                } else {
                    eventRepository.update(event)
                }
                _state.value = State.SAVED
            }
        }

    }

    private fun validateTrackCount(trackCountVal: Int?): Int? {
        if (trackCountVal == null) {
            return R.string.err_required_field
        }
        if (trackCountVal >= 5 || trackCountVal <= 0) {
            return R.string.err_value_must_be_between_1_and_5
        }
        return null
    }

    private fun validateTrackMaxTime(trackMaxTimeVal: Int?): Int? {
        if (trackMaxTimeVal == null) {
            return R.string.err_required_field
        }
        if (trackMaxTimeVal < 1) {
            return R.string.err_value_must_be_gt_0
        }
        return null
    }

    private fun newEvent(): Event {
        // Текущая дата + 1 день в 11:00
        val calendar = GregorianCalendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 11)
        calendar.set(Calendar.MINUTE, 0)
        return Event(null, calendar.time, 1, 2)
    }

    fun setEventDate(newDate: Date) {
        val date = eventDate.value
        if (date != null) {
            val newCalendar = GregorianCalendar.getInstance()
            newCalendar.time = newDate
            val calendar = GregorianCalendar.getInstance()
            calendar.time = date
            newCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            newCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
            eventDate.value = newCalendar.time
        }
    }

    fun setEventTime(hours: Int, minutes: Int) {
        val date = eventDate.value
        if (date != null) {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, hours)
            calendar.set(Calendar.MINUTE, minutes)
            eventDate.value = calendar.time
        }
    }

    fun getTime(): Pair<Int, Int> {
        val date = eventDate.value
        return if (date != null) {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = date
            Pair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        } else {
            Pair(11, 0)
        }
    }

    enum class State {
        SAVED,
        SAVING,
        EDITING
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
    }

}