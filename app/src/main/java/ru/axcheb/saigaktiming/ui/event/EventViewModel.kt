package ru.axcheb.saigaktiming.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.ddmmyyyyhhmmStr
import ru.axcheb.saigaktiming.data.model.db.Event
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class EventViewModel(
    private val memberRepository: MemberRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    val eventShare =
        eventRepository.getCurrentEvent().distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.Lazily, 0)
    val eventState = eventShare.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val eventDateTimeStr = eventState.map {
        it?.date?.ddmmyyyyhhmmStr()
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val trackCountStr = eventState.filterNotNull().map {
        "${it.currentTrack + 1}/${it.trackCount}"
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val members = eventState.filterNotNull().distinctUntilChanged().flatMapLatest {
        memberRepository.getEventMemberItems(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val progress =
        combine(eventState, members) { e, m -> calculateProgress(e, m) }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            0
        )

    private fun calculateProgress(event: Event?, members: List<EventMemberItem>): Int {
        if (event == null) return 0
        val inTrackProgress = if (members.isEmpty()) {
            0
        } else {
            100 * event.currentMemberIndex / members.size
        }
        // inTrackProgress == 100 только если все участники закончили соревнование
        // if добавлен, чтоб не было проблем с округлением.
        return if (inTrackProgress == 100) 100 else 100 * event.currentTrack / event.trackCount + inTrackProgress / event.trackCount
    }

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