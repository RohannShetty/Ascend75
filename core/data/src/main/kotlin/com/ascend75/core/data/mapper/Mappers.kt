package com.ascend75.core.data.mapper

import com.ascend75.core.database.dao.VaultPhotoProjection
import com.ascend75.core.database.entities.ChallengeInstanceEntity
import com.ascend75.core.database.entities.DailyRecordEntity
import com.ascend75.core.database.entities.ProgressPhotoEntity
import com.ascend75.core.database.entities.ReadingSessionEntity
import com.ascend75.core.database.entities.ScienceCardEntity
import com.ascend75.core.database.entities.TaskEntryEntity
import com.ascend75.core.database.entities.WaterLogEntity
import com.ascend75.core.database.entities.WorkoutSessionEntity
import com.ascend75.core.domain.model.ChallengeInstance
import com.ascend75.core.domain.model.ChallengeMode
import com.ascend75.core.domain.model.ChallengeStatus
import com.ascend75.core.domain.model.DailyRecord
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.model.ReadingSession
import com.ascend75.core.domain.model.ScienceCard
import com.ascend75.core.domain.model.TaskEntry
import com.ascend75.core.domain.model.VaultPhoto
import com.ascend75.core.domain.model.VaultPhotoRecord
import com.ascend75.core.domain.model.WaterLog
import com.ascend75.core.domain.model.WorkoutSession

fun ChallengeInstanceEntity.toDomain() = ChallengeInstance(
    id = id,
    attemptNumber = attemptNumber,
    mode = ChallengeMode.fromString(mode),
    status = ChallengeStatus.fromRaw(status),
    startedAt = startedAt,
    endedAt = endedAt,
    configJson = configJson,
    createdAt = createdAt
)

fun ChallengeInstance.toEntity() = ChallengeInstanceEntity(
    id = id,
    attemptNumber = attemptNumber,
    mode = mode.name,
    status = status.name,
    startedAt = startedAt,
    endedAt = endedAt,
    configJson = configJson,
    createdAt = createdAt
)

fun DailyRecordEntity.toDomain() = DailyRecord(
    id = id,
    challengeInstanceId = challengeInstanceId,
    dayNumber = dayNumber,
    calendarDate = calendarDate,
    isCompleted = isCompleted,
    sleepCutoffTimestamp = sleepCutoffTimestamp,
    reflectionNotes = reflectionNotes,
    createdAt = createdAt
)

fun TaskEntryEntity.toDomain() = TaskEntry(
    id = id,
    dailyRecordId = dailyRecordId,
    habitType = HabitType.fromRaw(habitType),
    isCompleted = isCompleted,
    completedAt = completedAt,
    notes = notes,
    targetValue = targetValue,
    currentValue = currentValue
)

fun TaskEntry.toEntity() = TaskEntryEntity(
    id = id,
    dailyRecordId = dailyRecordId,
    habitType = habitType.raw,
    isCompleted = isCompleted,
    completedAt = completedAt,
    notes = notes,
    targetValue = targetValue,
    currentValue = currentValue
)

fun WaterLogEntity.toDomain() = WaterLog(
    id = id,
    taskEntryId = taskEntryId,
    amountMl = amountMl,
    loggedAt = loggedAt
)

fun ReadingSessionEntity.toDomain() = ReadingSession(
    id = id,
    taskEntryId = taskEntryId,
    bookTitle = bookTitle,
    bookAuthor = bookAuthor,
    startPage = startPage,
    endPage = endPage,
    pagesRead = pagesRead,
    readingDurationSeconds = readingDurationSeconds,
    keyTakeaway = keyTakeaway,
    loggedAt = loggedAt
)

fun ReadingSession.toEntity() = ReadingSessionEntity(
    id = id,
    taskEntryId = taskEntryId,
    bookTitle = bookTitle,
    bookAuthor = bookAuthor,
    startPage = startPage,
    endPage = endPage,
    pagesRead = pagesRead,
    readingDurationSeconds = readingDurationSeconds,
    keyTakeaway = keyTakeaway,
    loggedAt = loggedAt
)

fun WorkoutSessionEntity.toDomain() = WorkoutSession(
    id = id,
    taskEntryId = taskEntryId,
    durationSeconds = durationSeconds,
    isOutdoor = isOutdoor,
    workoutType = workoutType,
    intensity = intensity,
    startedAt = startedAt,
    finishedAt = finishedAt,
    notes = notes
)

fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    id = id,
    taskEntryId = taskEntryId,
    durationSeconds = durationSeconds,
    isOutdoor = isOutdoor,
    workoutType = workoutType,
    intensity = intensity,
    startedAt = startedAt,
    finishedAt = finishedAt,
    notes = notes
)

fun VaultPhotoProjection.toDomain() = VaultPhoto(
    dayNumber = dayNumber,
    encryptedFilePath = encryptedFilePath,
    capturedAt = capturedAt
)

fun ProgressPhotoEntity.toDomain() = VaultPhotoRecord(
    id = id,
    taskEntryId = taskEntryId,
    encryptedFilePath = encryptedFilePath,
    photoHash = photoHash,
    fileSizeBytes = fileSizeBytes,
    capturedAt = capturedAt
)

fun ScienceCardEntity.toDomain() = ScienceCard(
    dayNumber = dayNumber,
    title = title,
    category = category,
    summary = summary,
    mechanism = mechanism,
    actionItem = actionItem,
    sourceCitation = sourceCitation,
    doiOrUrl = doiOrUrl,
    isBookmarked = isBookmarked
)
