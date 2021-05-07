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
    /** Дата и время старта. */
    var date: Date,
    /** Количество СУ. */
    @ColumnInfo(name = "track_count") var trackCount: Int,
    /** Время на прохождение СУ, через какое время стартует следующий участник. */
    @ColumnInfo(name = "track_max_time") val trackMaxTime: Int,
    @ColumnInfo(name = "in_archive") var inArchive: Boolean = false,

    /**
     * Порядковый номер СУ при запущенном соревновании. 0 (или 1) - соревнование не начато, -1 - все участники откатали и соревновние закончено.
     * 1 может быть в случае, если соревнование запустили, но ни один участник не проехал, а соревнование отменили.
     */
    @ColumnInfo(name = "current_track") var currentTrack: Int = 0,
    /** Порядковый номер последнего проеховшего СУ участника. 0 - соревнование не начато, -1 - все участники откатали и соревновние закончено. */
    @ColumnInfo(name = "current_member_number") var currentMemberNumber: Int = 0,

)