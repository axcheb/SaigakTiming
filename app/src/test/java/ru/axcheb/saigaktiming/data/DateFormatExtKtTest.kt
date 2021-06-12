package ru.axcheb.saigaktiming.data

import org.junit.Test

import org.junit.Assert.*

class DateFormatExtKtTest {

    @Test
    fun formatSecondsAndMs() {
        assertEquals(20094L.formatSecondsAndMs(), "20.094")
        assertEquals(10123L.formatSecondsAndMs(), "10.123")
        assertEquals(1003L.formatSecondsAndMs(), "1.003")
        assertEquals(2000L.formatSecondsAndMs(), "2.000")
        assertEquals(123456089L.formatSecondsAndMs(), "123456.089")
    }
}