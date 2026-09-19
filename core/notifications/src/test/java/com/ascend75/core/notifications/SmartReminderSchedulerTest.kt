package com.ascend75.core.notifications

import android.app.AlarmManager
import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class SmartReminderSchedulerTest {

    @Test
    fun verifyQuietHoursDetectionAcrossMidnight() {
        val context = mockk<Context>(relaxed = true)
        val scheduler = SmartReminderScheduler(context)

        // 23:30 is within quiet hours (22:00 to 07:00)
        val isQuietLate = scheduler.isWithinQuietHours(LocalTime.of(23, 30), 22, 7)
        assertTrue(isQuietLate)

        // 03:00 is within quiet hours (22:00 to 07:00)
        val isQuietEarly = scheduler.isWithinQuietHours(LocalTime.of(3, 0), 22, 7)
        assertTrue(isQuietEarly)

        // 14:00 is outside quiet hours
        val isQuietDay = scheduler.isWithinQuietHours(LocalTime.of(14, 0), 22, 7)
        assertFalse(isQuietDay)
    }
}
