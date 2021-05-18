package ru.axcheb.saigaktiming.data.model.dto

import androidx.room.*

@Entity(
    tableName = "member",
    indices = [Index("id")]
)
data class Member (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    var name: String
) {
    constructor(name: String) : this(null, name)
}