package com.blockme.app

import com.blockme.core.common.formatTimeWithAmPm
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.model.RepeatType
import com.blockme.feature.schedule.formattedDays
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for schedule time formatting and day repetition formatting.
 * SPDX-License-Identifier: MIT
 */
class ScheduleFormatTest {

    @Test
    fun `midnight formats as 12_00 AM`() {
        assertEquals("12:00 AM", formatTimeWithAmPm(0, 0))
    }

    @Test
    fun `early morning formats with AM`() {
        assertEquals("09:05 AM", formatTimeWithAmPm(9, 5))
        assertEquals("01:30 AM", formatTimeWithAmPm(1, 30))
        assertEquals("11:59 AM", formatTimeWithAmPm(11, 59))
    }

    @Test
    fun `noon formats as 12_00 PM`() {
        assertEquals("12:00 PM", formatTimeWithAmPm(12, 0))
    }

    @Test
    fun `afternoon and evening formats with PM`() {
        assertEquals("01:00 PM", formatTimeWithAmPm(13, 0))
        assertEquals("09:45 PM", formatTimeWithAmPm(21, 45))
        assertEquals("11:59 PM", formatTimeWithAmPm(23, 59))
    }

    @Test
    fun `all 7 days format as Every day`() {
        val schedule = FocusSchedule(
            hourOfDay = 9,
            minuteOfHour = 0,
            durationMs = 30 * 60 * 1000L,
            customDays = setOf(
                Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
            )
        )
        assertEquals("Every day", schedule.formattedDays())
    }

    @Test
    fun `weekdays format as Mon - Fri`() {
        val schedule = FocusSchedule(
            hourOfDay = 9,
            minuteOfHour = 0,
            durationMs = 30 * 60 * 1000L,
            customDays = setOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY
            )
        )
        assertEquals("Mon - Fri", schedule.formattedDays())
    }

    @Test
    fun `weekends format as Weekends`() {
        val schedule = FocusSchedule(
            hourOfDay = 9,
            minuteOfHour = 0,
            durationMs = 30 * 60 * 1000L,
            customDays = setOf(Calendar.SATURDAY, Calendar.SUNDAY)
        )
        assertEquals("Weekends", schedule.formattedDays())
    }

    @Test
    fun `custom selected days format as comma separated names in order`() {
        val schedule = FocusSchedule(
            hourOfDay = 9,
            minuteOfHour = 0,
            durationMs = 30 * 60 * 1000L,
            customDays = setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
        )
        assertEquals("Mon, Wed, Fri", schedule.formattedDays())
    }
}
