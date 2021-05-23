package ru.axcheb.saigaktiming.ui.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.axcheb.saigaktiming.data.repository.ResultRepository

class ProtocolViewModel(
    val eventId: Long,
    val resultRepository: ResultRepository
) : ViewModel() {

    val protocolItems = resultRepository.getProtocolItems(eventId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

}