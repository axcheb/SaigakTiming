package ru.axcheb.saigaktiming.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import java.text.SimpleDateFormat
import java.util.*

class EventViewModel(
    private val memberRepository: MemberRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    val eventShare =
        eventRepository.getCurrentEvent().shareIn(viewModelScope, SharingStarted.Lazily, 1)
    val eventState = eventShare.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val eventDateTimeStr = eventShare.map {
        val date = it?.date
        if (date == null) null else dateTimeFormat.format(date)
    }.asLiveData()

    val trackCountStr = eventShare.map {
        it?.trackCount?.toString()
    }.asLiveData()

    private val membersWithoutStartTimes =
        eventShare.map { it?.id }.filterNotNull().distinctUntilChanged()
            .flatMapLatest {
                memberRepository.getEventMemberItems(it)
            }

    val members = combine(eventShare, membersWithoutStartTimes) { event, members ->
        if (event != null) {
            members.onEach { it.calculateStartDates(event, members.size) }
        }
        members
    }.asLiveData()

    companion object {
        private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    }

}