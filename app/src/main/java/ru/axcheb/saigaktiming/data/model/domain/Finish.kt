package ru.axcheb.saigaktiming.data.model.domain

import androidx.room.*
import java.util.*

@Entity(
    tableName = "finish",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Start::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("start_id")
        ),
    ),
    indices = [Index("id"), Index("start_id")]
)
data class Finish(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "start_id") val startId: Long,
    var time: Date,
    @ColumnInfo(name = "sensor_id") var sensorId: Int,
    @ColumnInfo(name = "is_active") var isActive: Boolean
)