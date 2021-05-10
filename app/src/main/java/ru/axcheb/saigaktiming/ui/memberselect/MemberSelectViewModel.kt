package ru.axcheb.saigaktiming.ui.memberselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class MemberSelectViewModel(
    private val memberRepository: MemberRepository,
    eventRepository: EventRepository,
    val eventId: Long
): ViewModel() {

    private val event = eventRepository.getEvent(eventId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val members = memberRepository.getMemberSelected(eventId).asLiveData()

    fun handleBind(memberId: Long) {
        viewModelScope.launch {
            val eventMember = memberRepository.getEventMember(eventId, memberId).firstOrNull()
            if (eventMember == null) {
                memberRepository.bindToEvent(eventId, memberId)
            } else {
                val eventVal = event.value
                if (eventVal != null && !eventVal.isLaunched) {
                    memberRepository.subtractSequenceNumberAndUnbind(eventId, eventMember.sequenceNumber, memberId)
                }
            }
        }
    }

}