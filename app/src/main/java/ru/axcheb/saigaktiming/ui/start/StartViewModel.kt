package ru.axcheb.saigaktiming.ui.start

import android.text.format.DateUtils
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.model.dto.EventMemberCrossRef
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import ru.axcheb.saigaktiming.data.repository.ResultRepository
import ru.axcheb.saigaktiming.data.model.dto.Start

class StartViewModel(
    private val eventId: Long,
    private val memberId: Long,
    private val memberRepository: MemberRepository,
    private val resultRepository: ResultRepository
) : ViewModel() {

    val member = memberRepository.getMember(memberId).take(1)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private var eventMember: EventMemberCrossRef? = null

    val resultItems = resultRepository.getStartResults(eventId, memberId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val penaltySeconds: MutableStateFlow<Int?> = MutableStateFlow(null)

    val totalTimeStr = resultItems.map { items ->
        val sum = items.filter { it.isActive }.fold(0L) { sum, item -> sum + item.diff }
        "${DateUtils.formatElapsedTime(sum / 1000)}.${sum % 1000}"
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    init {
        viewModelScope.launch {
            eventMember = memberRepository.getEventMember(eventId, memberId).firstOrNull()
            penaltySeconds.value = eventMember?.penaltySeconds ?: 0
        }

        viewModelScope.launch {
            penaltySeconds.collect {
                val penalty = it ?: 0
                val em = eventMember
                if (em != null) {
                    if (penalty != em.penaltySeconds) {
                        em.penaltySeconds = penalty
                        memberRepository.updateEventMemberCrossRef(em)
                    }
                }
            }
        }
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