package ru.axcheb.saigaktiming.ui.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.axcheb.saigaktiming.data.mapper.SensorMessageMapper
import ru.axcheb.saigaktiming.data.model.dto.Settings
import ru.axcheb.saigaktiming.data.repository.SettingsRepository
import ru.axcheb.saigaktiming.service.BluetoothSerialBoardService

class SensorsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val isImitationMode = MutableStateFlow(false)

    val sensorsText = MutableStateFlow("")

    private var settings: Settings? = null

    private val sensorMessageMapper = SensorMessageMapper()

    init {
        viewModelScope.launch {
            settings = settingsRepository.get().first()
            isImitationMode.value = settings?.isImitationMode ?: false
        }

        viewModelScope.launch {
            isImitationMode.collect {
                val settings0 = settings
                if (settings0 != null) {
                    settings0.isImitationMode = it
                    settingsRepository.update(settings0)
                }
            }
        }

        launchMessageListenJob()
    }

    private fun launchMessageListenJob() {
        viewModelScope.launch {
            BluetoothSerialBoardService.messageFlow.collect {
                val msg = sensorMessageMapper.map(it)
                sensorsText.value = "$it - $msg\n${sensorsText.value}"
            }
        }
    }

}