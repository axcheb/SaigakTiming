package ru.axcheb.saigaktiming.ui.event

import androidx.lifecycle.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.model.domain.Event
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository

class EventViewModel(
    private val memberRepository: MemberRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    val members: LiveData<List<EventMemberItem>> =  event.switchMap {
        val eventId = event.value?.id
        return@switchMap if (eventId != null) {
            memberRepository.getEventMemberItems(eventId).asLiveData()
        } else {
            MutableLiveData()
        }
    }

    init {
        viewModelScope.launch {
            // TODO может перенести в репозиторий? или в start, а сюда передвать только eventId при запуске активити
            _event.value = eventRepository.getCurrentEvent().firstOrNull()
            // check if there is no active event, then create new one:
            if (event.value == null) {
                _event.value = eventRepository.newEvent()
            }
        }
    }


}