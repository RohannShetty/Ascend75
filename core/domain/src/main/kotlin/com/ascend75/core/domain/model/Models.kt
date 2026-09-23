package com.ascend75.core.domain.model

/** Mirrors `ChallengeInstanceEntity`. */
data class ChallengeInstance(
    val id: String,
    val attemptNumber: Int,
    val mode: ChallengeMode,
    val status: ChallengeStatus,
    val startedAt: Long,
    val endedAt: Long? = null,
    val configJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ChallengeStatus {
    ACTIVE, COMPLETED, RESET_ARCHIVED;

    companion object {
        fun fromRaw(value: String): ChallengeStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

/** Mirrors `DailyRecordEntity`. */
data class DailyRecord(
    val id: String,
    val challengeInstanceId: String,
    val dayNumber: Int,
    val calendarDate: Long,
    val isCompleted: Boolean,
    val sleepCutoffTimestamp: Long,
    val reflectionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** Mirrors `TaskEntryEntity`, with the habit type typed. */
data class TaskEntry(
    val id: String,
    val dailyRecordId: String,
    val habitType: HabitType,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null,
    val targetValue: Double = 1.0,
    val currentValue: Double = 0.0
)

/** Mirrors `WaterLogEntity`. */
data class WaterLog(
    val id: String,
    val taskEntryId: String,
    val amountMl: Int,
    val loggedAt: Long
)

/** Mirrors `ReadingSessionEntity`. */
data class ReadingSession(
    val id: String,
    val taskEntryId: String,
    val bookTitle: String,
    val bookAuthor: String? = null,
    val startPage: Int,
    val endPage: Int,
    val pagesRead: Int,
    val readingDurationSeconds: Int? = null,
    val keyTakeaway: String? = null,
    val loggedAt: Long
)

/** Mirrors `WorkoutSessionEntity`. */
data class WorkoutSession(
    val id: String,
    val taskEntryId: String,
    val durationSeconds: Int,
    val isOutdoor: Boolean,
    val workoutType: String,
    val intensity: String,
    val startedAt: Long,
    val finishedAt: Long,
    val notes: String? = null
)

/** Mirrors `ProgressPhotoDao.VaultPhotoProjection` -- a day-scoped vault row for the UI. */
data class VaultPhoto(
    val dayNumber: Int,
    val encryptedFilePath: String,
    val capturedAt: Long
)

/** Mirrors `ProgressPhotoEntity` in full; the export path needs every column. */
data class VaultPhotoRecord(
    val id: String,
    val taskEntryId: String,
    val encryptedFilePath: String,
    val photoHash: String,
    val fileSizeBytes: Long,
    val capturedAt: Long
)

/** Mirrors `ScienceCardEntity`. */
data class ScienceCard(
    val dayNumber: Int,
    val title: String,
    val category: String,
    val summary: String,
    val mechanism: String,
    val actionItem: String,
    val sourceCitation: String,
    val doiOrUrl: String? = null,
    val isBookmarked: Boolean = false
)

/** Moved from :core:datastore so the domain owns the settings contract. */
data class UserPreferences(
    val isOnboardingCompleted: Boolean = false,
    val activeChallengeId: String? = null,
    val wakeHour: Int = 6,
    val wakeMinute: Int = 30,
    val sleepCutoffHour: Int = 3,
    val sleepCutoffMinute: Int = 0,
    val selectedMode: String = "STRICT_75",
    val isBiometricEnabled: Boolean = false,
    val quietHoursStartHour: Int = 22,
    val quietHoursEndHour: Int = 7,
    val lastCelebratedDay: Int = 0
)

/** Everything the UI needs about the challenge day currently in progress. Moved from :feature:dashboard. */
data class TodayProtocol(
    val challenge: ChallengeInstance,
    val record: DailyRecord,
    val tasks: List<TaskEntry>,
    val prefs: UserPreferences,
    val mode: ChallengeMode,
    val isPastCutoff: Boolean,
    val streakDays: Int
)
