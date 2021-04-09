package ru.axcheb.saigaktiming.ui.start

import android.text.format.DateUtils
import androidx.lifecycle.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import ru.axcheb.saigaktiming.data.repository.ResultRepository
import ru.axcheb.saigaktiming.data.model.domain.Start

class StartViewModel(
    private val eventId: Long,
    private val memberId: Long,
    private val memberRepository: MemberRepository,
    private val resultRepository: ResultRepository
) : ViewModel() {

    val member = memberRepository.getMember(memberId).asLiveData()

    val resultItems = resultRepository.getStartResults(eventId, memberId).asLiveData()

    val totalTimeStr = resultItems.map { items ->
        val sum = items.filter { it.isActive } .fold(0L) { sum, item -> sum + item.diff }
        "${DateUtils.formatElapsedTime(sum / 1000)}.${sum % 1000}"
    }

    /**
     * Меняет is_active для [Start] на противоположный.
     */
    fun handleStartActive(startId: Long) {
        viewModelScope.launch {
            val start = resultRepository.getStart(startId).firstOrNull() ?: return@launch
            val newStatus = !start.isActive
            if (newStatus) {
                resultRepository.activateStart(startId)
            } else {
                resultRepository.inactivateStart(startId)
            }
        }
    }

}