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
    var date: Date,
    @ColumnInfo(name = "in_archive") var inArchive: Boolean = false
)