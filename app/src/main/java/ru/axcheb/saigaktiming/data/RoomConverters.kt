package ru.axcheb.saigaktiming.data

import androidx.room.TypeConverter
import java.util.*

class RoomConverters {
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun timestampToDate(value: Long?): Date? = value?.let { Date(it) }

}