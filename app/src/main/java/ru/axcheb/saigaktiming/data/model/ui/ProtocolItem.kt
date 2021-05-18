package ru.axcheb.saigaktiming.data.model.ui

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

/** Модель протокола соревнования. */
data class ProtocolItem(
    // From EventMember:
    val eventMemberId: Long,
    val sequenceNumber: Int,
    val penaltySeconds: Int,
    // From Member:
    val memberId: Long,
    val memberName: String,

    /** Результат в миллисекундах. */
    val resultMillis: Long,
    /** Результаты на каждом СУ:  */
    val trackResults: List<ProtocolFinishItem>

) {

    /** Format string like "01:21.131(1), 00:58.867(2), 01:21.131(1)" */
    fun getStartTimesStr() = trackResults.joinToString {
        "${DateUtils.formatElapsedTime(it.resultMillis / 1000)}.${it.resultMillis % 1000}(${it.sensorId})"
    }

    fun getResultTimeStr() =
        "${DateUtils.formatElapsedTime(resultMillis / 1000)}.${resultMillis % 1000}"

    fun isPenaltyVisible() = penaltySeconds > 0

}
