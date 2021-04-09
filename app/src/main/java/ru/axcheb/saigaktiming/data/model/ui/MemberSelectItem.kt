package ru.axcheb.saigaktiming.data.model.ui

import androidx.room.ColumnInfo

data class MemberSelectItem(
    @ColumnInfo(name = "member_id") val memberId: Long,
    val name: String,
    @ColumnInfo(name = "sequence_number") val sequenceNumber: Int?
) {
    fun getSequenceNumber(): String {
        return sequenceNumber?.toString() ?: ""
    }
}
