package ru.axcheb.saigaktiming.data.model.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "event",
    indices = [Index("id")]
)
data class Event(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    /** Дата и время старта. */
    var date: Date,
    /** Количество СУ. */
    @ColumnInfo(name = "track_count") var trackCount: Int,
    /** Время на прохождение СУ, через какое время стартует следующий участник. */
    @ColumnInfo(name = "track_max_time") val trackMaxTime: Int,
    @ColumnInfo(name = "is_in_archive") var isInArchive: Boolean = false,
    /** Запущено ли соревнование. При этом статусе нельзя удалять участников. Добавлять можно. */
    @ColumnInfo(name = "is_launched") var isLaunched: Boolean = false,
    /**
     * Порядковый номер текущего СУ при запущенном соревновании. Начинается с 0.
     */
    @ColumnInfo(name = "current_track") var currentTrack: Int = 0,
    /**
     * Порядковый номер участника, которого надо запустить на СУ. Начинается с 0.
     * Если currentMemberIndex = members.size и currentTrack =  все участники откатали и соревновние закончено.
     */
    @ColumnInfo(name = "current_member_index") var currentMemberIndex: Int = 0,
    /** Автопауза между СУ. */
    @ColumnInfo(name = "is_auto_pause_between_tracks", defaultValue = "0")
    var isAutoPauseBetweenTracks: Boolean = false
) {

    fun getTrackTimeMillis(): Long = trackMaxTime * 1_000 * 60L

}