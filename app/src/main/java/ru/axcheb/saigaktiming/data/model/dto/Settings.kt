package ru.axcheb.saigaktiming.data.model.dto

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