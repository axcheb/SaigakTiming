package ru.axcheb.saigaktiming.data.mapper

import android.util.Log
import ru.axcheb.saigaktiming.data.model.dto.SensorMessage
import java.util.*

class SensorMessageMapper : Mapper<String, SensorMessage?> {
    private val TAG = this::class.qualifiedName

    override fun map(input: String): SensorMessage? {
        val split = input.split(',')
        if (split.size == 4) {
            try {
                val date = Date(split[2].toLong() * 1000 + split[3].toLong())
                return SensorMessage(split[0], split[1].toInt(), date)
            } catch (ex: Exception) {
                // do nothing
                Log.e(TAG, "Cant parse string: \"$input\"", ex)
            }
        }
        return null
    }
}