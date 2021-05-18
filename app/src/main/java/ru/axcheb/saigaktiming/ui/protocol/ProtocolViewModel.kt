package ru.axcheb.saigaktiming.ui.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.model.ui.ProtocolItem
import ru.axcheb.saigaktiming.data.repository.ResultRepository

class ProtocolViewModel(
    private val eventId: Long,
    private val resultRepository: ResultRepository
) : ViewModel() {

    private val _protocolItems: MutableStateFlow<List<ProtocolItem>> = MutableStateFlow(emptyList())
    val protocolItems: StateFlow<List<ProtocolItem>> = _protocolItems

    init {
        viewModelScope.launch {
            _protocolItems.value = resultRepository.getProtocolItems(eventId)
        }
    }

}