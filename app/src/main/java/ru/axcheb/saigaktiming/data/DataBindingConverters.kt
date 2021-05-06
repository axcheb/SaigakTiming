package ru.axcheb.saigaktiming.data

import androidx.databinding.InverseMethod
import java.lang.Integer.parseInt
import java.lang.NumberFormatException

class DataBindingConverters {

    companion object {
        @JvmStatic
        fun intToStr(value: Int?): String? {
            return value?.toString()
        }

        @InverseMethod("intToStr")
        @JvmStatic
        fun strToInt(value: String?): Int? {
            return try {
                parseInt(value)
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}