package ru.axcheb.saigaktiming.ui.memberselect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.domain.Member
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class NewMemberViewModel(
    private val memberRepository: MemberRepository,
    private val eventId: Long
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    private val _errorMsg = MutableLiveData<Int>()
    val errorMsg: LiveData<Int> = _errorMsg

    private val _addResult = MutableLiveData<AddResult>()
    val addResult = _addResult

    val name = MutableLiveData<String>()

    fun addNewMember() {
        viewModelScope.launch {
            val value = name.value?.trim()
            if (!value.isNullOrEmpty()) {
                val oldMember = memberRepository.getMember(value).firstOrNull()
                if (oldMember != null) {
                    _addResult.postValue(AddResult.ALREADY_EXISTS)
                    _errorMsg.postValue(R.string.member_already_exists)
                } else {
                val member = Member(name = value)
                    memberRepository.saveNewAndBind(member, eventId)
                    _addResult.value = AddResult.OK
                    _errorMsg.postValue(null)
                    name.postValue(null)
                    _addResult.postValue(null)
                }
            }
        }
    }
}

enum class AddResult {
    OK,
    ALREADY_EXISTS,
}