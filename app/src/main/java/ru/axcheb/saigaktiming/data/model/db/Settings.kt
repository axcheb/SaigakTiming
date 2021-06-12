package ru.axcheb.saigaktiming.data.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings",
)
data class Settings (
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "is_imitation_mode") var isImitationMode: Boolean
)