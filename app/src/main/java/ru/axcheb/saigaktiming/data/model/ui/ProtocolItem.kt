package ru.axcheb.saigaktiming.data.model.ui

import ru.axcheb.saigaktiming.data.formatElapsedTimeMs

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
        "${it.resultMillis.formatElapsedTimeMs()} (${it.sensorId})"
    }

    fun getResultTimeStr() = resultMillis.formatElapsedTimeMs()

    fun isPenaltyVisible() = penaltySeconds > 0

}
