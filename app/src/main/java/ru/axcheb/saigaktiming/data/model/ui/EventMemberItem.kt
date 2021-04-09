package ru.axcheb.saigaktiming.data.model.ui

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class EventMemberItem (
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "member_id") val memberId: Long,
    @ColumnInfo(name = "sequence_number") var sequenceNumber: Int,
    var name: String

)