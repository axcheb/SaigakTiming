package ru.axcheb.saigaktiming.data.model.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "event",
    indices = [Index("id")]
)
data class Event (
    @PrimaryKey(autoGenerate = true) var id: Long?,
    /** Дата и время старта. */
    var date: Date,
    /** Количество СУ. */
    @ColumnInfo(name = "track_count") var trackCount: Int,
    /** Время на прохождение СУ, через какое время стартует следующий участник. */
    @ColumnInfo(name = "track_max_time") val trackMaxTime: Int,
    @ColumnInfo(name = "in_archive") var inArchive: Boolean = false
)