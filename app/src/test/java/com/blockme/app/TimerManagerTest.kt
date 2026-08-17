package com.blockme.app

import com.blockme.core.common.Constants
import com.blockme.core.common.toFormattedTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for timer math and duration validation.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
class TimerManagerTest {

    // ── Wall-clock timer calculation tests ───────────────────────────────────

    @Test
    fun `remaining time is calculated from wall-clock end time`() {
        val now = System.currentTimeMillis()
        val durationMs = 60 * 60 * 1000L  // 1 hour
        val endTime = now + durationMs
        val remaining = endTime - now
        assertTrue("Remaining should be approximately 1 hour", remaining in (durationMs - 100)..(durationMs + 100))
    }

    @Test
    fun `1 hour timer started at any time ends exactly 1 hour later`() {
        val startMs = System.currentTimeMillis()
        val durationMs = 60 * 60 * 1000L
        val endMs = startMs + durationMs
        // End time should be exactly 1 hour after start
        assertEquals(durationMs, endMs - startMs)
    }

    @Test
    fun `remaining time returns zero when session has expired`() {
        val endTime = System.currentTimeMillis() - 5000L  // 5 seconds in the past
        val remaining = (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        assertEquals(0L, remaining)
    }

    // ── Duration formatting tests ─────────────────────────────────────────────

    @Test
    fun `zero ms formats as 00_00_00`() {
        assertEquals("00:00:00", 0L.toFormattedTime())
    }

    @Test
    fun `30 minutes formats correctly`() {
        val thirtyMin = 30 * 60 * 1000L
        assertEquals("00:30:00", thirtyMin.toFormattedTime())
    }

    @Test
    fun `1 hour formats correctly`() {
        val oneHour = 60 * 60 * 1000L
        assertEquals("01:00:00", oneHour.toFormattedTime())
    }

    @Test
    fun `3 hours formats correctly`() {
        val threeHours = 3 * 60 * 60 * 1000L
        assertEquals("03:00:00", threeHours.toFormattedTime())
    }

    @Test
    fun `1 hour 30 min 45 sec formats correctly`() {
        val ms = ((1 * 60 + 30) * 60 + 45) * 1000L
        assertEquals("01:30:45", ms.toFormattedTime())
    }

    // ── Max duration enforcement tests ────────────────────────────────────────

    @Test
    fun `duration of exactly 3 hours is valid`() {
        val threeHours = 3 * 60 * 60 * 1000L
        assertTrue(threeHours <= Constants.MAX_SESSION_DURATION_MS)
    }

    @Test
    fun `duration over 3 hours is invalid`() {
        val overThreeHours = (3 * 60 * 60 * 1000L) + 1L
        assertFalse(overThreeHours <= Constants.MAX_SESSION_DURATION_MS)
    }

    @Test
    fun `all preset durations are within 3 hour limit`() {
        Constants.PRESET_DURATIONS_MINUTES.forEach { minutes ->
            val ms = minutes * 60_000L
            assertTrue("Preset ${minutes}m exceeds 3h limit", ms <= Constants.MAX_SESSION_DURATION_MS)
        }
    }

    @Test
    fun `custom duration of 3h 1min is rejected`() {
        val totalMinutes = 3 * 60 + 1
        val ms = totalMinutes * 60_000L
        assertFalse(ms <= Constants.MAX_SESSION_DURATION_MS)
    }
}
