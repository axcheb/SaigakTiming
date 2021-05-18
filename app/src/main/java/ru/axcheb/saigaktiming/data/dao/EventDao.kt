package ru.axcheb.saigaktiming.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.axcheb.saigaktiming.data.model.dto.Event

@Dao
interface EventDao {

    @Query(value = "select * from event where is_in_archive = 0")
    fun getCurrentEvent(): Flow<Event?>

    @Query(value = "select * from event where id = :id")
    fun getEvent(id: Long): Flow<Event>

    @Insert
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(vararg event: Event)

}