package com.ascend75.core.common.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Handles day transitions respecting user's configured sleep cutoff (default 3:00 AM)
 * rather than strict calendar midnight (12:00 AM).
 */
object DayBoundaryEvaluator {

    /**
     * Calculates the sleep cutoff timestamp for a specific calendar date and sleep cutoff time.
     * If sleep cutoff is 03:00 AM, the cutoff occurs on the morning of calendarDate + 1.
     */
    fun calculateSleepCutoffTimestamp(
        calendarDate: LocalDate,
        sleepCutoffTime: LocalTime = LocalTime.of(3, 0),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val nextMorning = calendarDate.plusDays(1)
        val cutoffDateTime = LocalDateTime.of(nextMorning, sleepCutoffTime)
        return cutoffDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    /**
     * Determines if current timestamp is past the day's sleep cutoff.
     */
    fun isPastSleepCutoff(
        currentTimestamp: Long,
        sleepCutoffTimestamp: Long
    ): Boolean {
        return currentTimestamp > sleepCutoffTimestamp
    }

    /**
     * Maps an arbitrary timestamp to the effective challenge day date.
     * E.g. 1:30 AM on Oct 2 falls into the day window of Oct 1 if cutoff is 3:00 AM.
     */
    fun getEffectiveChallengeDate(
        timestamp: Long,
        sleepCutoffHour: Int = 3,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDate {
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
        return if (dateTime.hour < sleepCutoffHour) {
            dateTime.toLocalDate().minusDays(1)
        } else {
            dateTime.toLocalDate()
        }
    }
}
