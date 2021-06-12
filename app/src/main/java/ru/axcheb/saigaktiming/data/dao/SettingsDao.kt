package ru.axcheb.saigaktiming.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.axcheb.saigaktiming.data.model.db.Settings

@Dao
interface SettingsDao {

    @Query("select * from settings where id = 1")
    fun get(): Flow<Settings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: Settings)

    @Update
    suspend fun update(settings: Settings)

}