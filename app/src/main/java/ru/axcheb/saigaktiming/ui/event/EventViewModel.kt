package ru.axcheb.saigaktiming.ui.event

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import java.text.SimpleDateFormat
import java.util.*

class EventViewModel(
    private val memberRepository: MemberRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val TAG = this::class.qualifiedName

    val eventShare = eventRepository.getCurrentEvent().shareIn(viewModelScope, SharingStarted.Lazily, 1)
    val eventState = eventShare.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val eventDateTimeStr = eventShare.map {
        val date = it?.date
        if (date == null) null else dateTimeFormat.format(date)
    }.asLiveData()

    val trackCountStr = eventShare.map {
        it?.trackCount?.toString()
    }.asLiveData()

    val members: LiveData<List<EventMemberItem>> = eventShare.flatMapLatest {
        val eventId = it?.id
        if (eventId == null) {
            flow {  }
        } else {
            memberRepository.getEventMemberItems(eventId)
        }
    }.asLiveData()

    companion object {
        private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    }

}