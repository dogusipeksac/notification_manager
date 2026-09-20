package com.dogusipeksac.notificationscheduler.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class QuietHoursEvaluatorTest {

    @Test
    fun sameDayWindow_includesStart_excludesEnd() {
        // 09:00–17:00
        val start = 9 * 60
        val end = 17 * 60
        assertFalse(QuietHoursEvaluator.isInQuietHours(8 * 60 + 59, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(9 * 60, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(12 * 60, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(16 * 60 + 59, start, end))
        assertFalse(QuietHoursEvaluator.isInQuietHours(17 * 60, start, end))
        assertFalse(QuietHoursEvaluator.isInQuietHours(21 * 60, start, end))
    }

    @Test
    fun overnightWindow_coversEveningAndMorning() {
        // 22:00–08:00 (gece yarısını saran aralık)
        val start = 22 * 60
        val end = 8 * 60
        assertFalse(QuietHoursEvaluator.isInQuietHours(21 * 60 + 59, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(22 * 60, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(23 * 60 + 30, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(0, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(2 * 60, start, end))
        assertTrue(QuietHoursEvaluator.isInQuietHours(7 * 60 + 59, start, end))
        assertFalse(QuietHoursEvaluator.isInQuietHours(8 * 60, start, end))
        assertFalse(QuietHoursEvaluator.isInQuietHours(12 * 60, start, end))
    }

    @Test
    fun equalStartAndEnd_isNeverQuiet() {
        assertFalse(QuietHoursEvaluator.isInQuietHours(0, 480, 480))
        assertFalse(QuietHoursEvaluator.isInQuietHours(480, 480, 480))
        assertFalse(QuietHoursEvaluator.isInQuietHours(1320, 480, 480))
    }

    @Test
    fun nextQuietEnd_overnightEvening_isTomorrowMorning() {
        val now = cal(hour = 23, minute = 0)
        val endMillis = QuietHoursEvaluator.nextQuietEndMillis(22 * 60, 8 * 60, now)
        val expected = cal(hour = 8, minute = 0).apply { add(Calendar.DAY_OF_YEAR, 1) }
        assertEquals(expected.timeInMillis, endMillis)
    }

    @Test
    fun nextQuietEnd_overnightMorning_isTodayMorning() {
        val now = cal(hour = 2, minute = 15)
        val endMillis = QuietHoursEvaluator.nextQuietEndMillis(22 * 60, 8 * 60, now)
        val expected = cal(hour = 8, minute = 0)
        assertEquals(expected.timeInMillis, endMillis)
    }

    @Test
    fun nextQuietEnd_sameDay_isTodayEnd() {
        val now = cal(hour = 13, minute = 0)
        val endMillis = QuietHoursEvaluator.nextQuietEndMillis(12 * 60, 14 * 60, now)
        val expected = cal(hour = 14, minute = 0)
        assertEquals(expected.timeInMillis, endMillis)
    }

    @Test
    fun formatMinutes_padsHoursAndMinutes() {
        assertEquals("00:00", QuietHoursEvaluator.formatMinutes(0))
        assertEquals("08:00", QuietHoursEvaluator.formatMinutes(480))
        assertEquals("22:00", QuietHoursEvaluator.formatMinutes(1320))
        assertEquals("23:59", QuietHoursEvaluator.formatMinutes(23 * 60 + 59))
    }

    @Test
    fun isWeekend_saturdayAndSundayOnly() {
        // 2026-06-15 Monday
        assertFalse(QuietHoursEvaluator.isWeekend(cal(hour = 12, minute = 0)))
        // 2026-06-20 Saturday
        assertTrue(QuietHoursEvaluator.isWeekend(dayCal(2026, Calendar.JUNE, 20)))
        // 2026-06-21 Sunday
        assertTrue(QuietHoursEvaluator.isWeekend(dayCal(2026, Calendar.JUNE, 21)))
        // 2026-06-19 Friday
        assertFalse(QuietHoursEvaluator.isWeekend(dayCal(2026, Calendar.JUNE, 19)))
    }

    private fun cal(hour: Int, minute: Int): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 15, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun dayCal(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(year, month, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
