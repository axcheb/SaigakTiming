package ru.axcheb.saigaktiming.data.model.dto

import androidx.room.*
import java.util.*

@Entity(
    tableName = "start",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EventMemberCrossRef::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("event_member_id")
        ),
    ),
    indices = [Index("id"), Index("event_member_id")]
)
data class Start(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "event_member_id") val eventMemberId: Long,
    var time: Date,
    @ColumnInfo(name = "is_active") var isActive: Boolean
)