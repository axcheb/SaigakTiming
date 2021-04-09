package ru.axcheb.saigaktiming.data.model.domain

import androidx.room.*

@Entity(
    tableName = "event_member",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Event::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("event_id")
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("member_id")
        ),
    ),
    indices = [Index("id"), Index("event_id"), Index("member_id")]
)
data class EventMember(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "event_id") val eventId: Long?,
    @ColumnInfo(name = "member_id") val memberId: Long?,
    @ColumnInfo(name = "sequence_number") var sequenceNumber: Int
)