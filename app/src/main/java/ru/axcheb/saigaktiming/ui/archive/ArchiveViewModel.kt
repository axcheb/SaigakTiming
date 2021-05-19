package ru.axcheb.saigaktiming.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.axcheb.saigaktiming.data.repository.EventRepository

class ArchiveViewModel(eventRepository: EventRepository) : ViewModel() {

    val archivedItems = eventRepository.getArchivedItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

}