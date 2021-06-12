package ru.axcheb.saigaktiming.data.repository

import ru.axcheb.saigaktiming.data.dao.SettingsDao
import ru.axcheb.saigaktiming.data.model.db.Settings

class SettingsRepository(private val settingsDao: SettingsDao) {

    fun get() = settingsDao.get()

    suspend fun update(settings: Settings) = settingsDao.update(settings)

}