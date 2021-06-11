package ru.axcheb.saigaktiming.ui.memberselect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.dto.Member
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class NewMemberViewModel(
    private val memberRepository: MemberRepository,
    private val eventId: Long
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg get(): LiveData<Int?> = _errorMsg

    private val _state = MutableLiveData(State.EDITING)
    val state get(): LiveData<State> = _state

    val name = MutableLiveData<String?>()

    fun addNewMember() {
        viewModelScope.launch {
            val value = name.value?.trim()
            if (!value.isNullOrEmpty()) {
                val oldMember = memberRepository.getMember(value).firstOrNull()
                if (oldMember != null) {
                    _state.value = State.ALREADY_EXISTS
                    _errorMsg.value = R.string.err_member_already_exists
                    _state.value = State.EDITING
                } else {
                val member = Member(name = value)
                    _state.value = State.SAVING
                    memberRepository.saveNewAndBind(member, eventId)
                    _state.value = State.SAVED
                    _errorMsg.value = null
                    name.value = null
                    _state.value = State.EDITING
                }
            }
        }
    }

    enum class State {
        SAVED,
        SAVING,
        ALREADY_EXISTS,
        EDITING
    }
}

