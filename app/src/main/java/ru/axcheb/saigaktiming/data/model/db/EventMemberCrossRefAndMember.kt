package ru.axcheb.saigaktiming.data.model.db

import androidx.room.Embedded
import androidx.room.Relation

data class EventMemberCrossRefAndMember (
    @Embedded val eventMemberCrossRef: EventMemberCrossRef,
    @Relation (
        parentColumn = "member_id",
        entityColumn = "id"
    )
    val member: Member
)