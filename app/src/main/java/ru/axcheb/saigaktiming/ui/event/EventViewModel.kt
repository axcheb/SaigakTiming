package ru.axcheb.saigaktiming.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.ddmmyyyyhhmmStr
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class EventViewModel(
    private val memberRepository: MemberRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    val eventShare =
        eventRepository.getCurrentEvent().distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.Lazily, 0)
    val eventState = eventShare.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val eventDateTimeStr = eventState.map {
        it?.date?.ddmmyyyyhhmmStr()
    }.asLiveData()

    val trackCountStr = eventState.map {
        it?.trackCount?.toString()
    }.asLiveData()

    private val membersWithoutStartTimes =
        eventState.map { it?.id }.filterNotNull().distinctUntilChanged()
            .flatMapLatest {
                memberRepository.getEventMemberItems(it)
            }

    val members = combine(eventState, membersWithoutStartTimes) { event, members ->
        if (event != null) {
            members.onEach { it.calculateStartDates(event, members.size) }
        }
        members
    }.asLiveData()

    fun toArchive() {
        val event = eventState.value
        if (event != null) {
            viewModelScope.launch {
                event.isInArchive = true
                eventRepository.update(event)
            }
        }
    }

}