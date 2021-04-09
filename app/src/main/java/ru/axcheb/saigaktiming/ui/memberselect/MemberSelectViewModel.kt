package ru.axcheb.saigaktiming.ui.memberselect

import androidx.lifecycle.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.model.domain.Member
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class MemberSelectViewModel(
    private val memberRepository: MemberRepository,
    val eventId: Long
): ViewModel() {

    val members = memberRepository.getMemberSelected(eventId).asLiveData()

    fun handleBind(memberId: Long) {
        viewModelScope.launch {
            memberRepository.handleEventMemberBind(eventId, memberId)
        }
    }

}