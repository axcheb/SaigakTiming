package ru.axcheb.saigaktiming.data.mapper

import ru.axcheb.saigaktiming.data.model.db.SensorMessage
import timber.log.Timber
import java.util.*

class SensorMessageMapper : Mapper<String, SensorMessage?> {

    override fun map(input: String): SensorMessage? {
        val split = input.split(',')
        if (split.size == 4) {
            try {
                val date = Date(split[2].toLong() * 1000 + split[3].toLong())
                return SensorMessage(split[0], split[1].toInt(), date)
            } catch (ex: Exception) {
                Timber.e(ex,"Cant parse string: \"$input\"")
            }
        }
        return null
    }
}