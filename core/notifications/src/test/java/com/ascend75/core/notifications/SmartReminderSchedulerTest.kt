package com.ascend75.core.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartReminderSchedulerTest {

    @Test
    fun quietHoursWindowCrossingMidnightCoversLateEveningAndEarlyMorning() {
        // 22:00 -> 07:00 wraps midnight.
        assertTrue(SmartReminderScheduler.isQuietHour(23, quietStartHour = 22, quietEndHour = 7))
        assertTrue(SmartReminderScheduler.isQuietHour(3, quietStartHour = 22, quietEndHour = 7))
        assertFalse(SmartReminderScheduler.isQuietHour(14, quietStartHour = 22, quietEndHour = 7))
    }

    @Test
    fun nonWrappingWindowIsInclusiveOfItsStartAndExclusiveOfItsEnd() {
        assertTrue(SmartReminderScheduler.isQuietHour(12, quietStartHour = 12, quietEndHour = 15))
        assertTrue(SmartReminderScheduler.isQuietHour(14, quietStartHour = 12, quietEndHour = 15))
        assertFalse(SmartReminderScheduler.isQuietHour(15, quietStartHour = 12, quietEndHour = 15))
        assertFalse(SmartReminderScheduler.isQuietHour(11, quietStartHour = 12, quietEndHour = 15))
    }

    @Test
    fun defaultsMatchTheDocumentedTenPmToSevenAmWindow() {
        assertTrue(SmartReminderScheduler.isQuietHour(22))
        assertTrue(SmartReminderScheduler.isQuietHour(6))
        assertFalse(SmartReminderScheduler.isQuietHour(7))
        assertFalse(SmartReminderScheduler.isQuietHour(21))
    }
}
