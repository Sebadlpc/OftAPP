package com.example.oftapp.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun dateToTimestamp_and_fromTimestamp_areConsistent() {
        val now = Date()
        val timestamp = converters.dateToTimestamp(now)
        val dateFromTimestamp = converters.fromTimestamp(timestamp)

        assertEquals(now.time, timestamp)
        assertEquals(now, dateFromTimestamp)
    }

    @Test
    fun nullHandling_returnsNull() {
        assertNull(converters.dateToTimestamp(null))
        assertNull(converters.fromTimestamp(null))
        assertNull(converters.fromString(null))
        assertNull(converters.uriToString(null))
    }
}
