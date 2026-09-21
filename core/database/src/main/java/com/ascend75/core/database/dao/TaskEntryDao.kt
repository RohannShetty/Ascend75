package com.ascend75.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ascend75.core.database.entities.TaskEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskEntries(tasks: List<TaskEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskEntry(task: TaskEntryEntity)

    @Update
    suspend fun updateTaskEntry(task: TaskEntryEntity)

    @Query("SELECT * FROM task_entries WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntryEntity?

    @Query("SELECT * FROM task_entries WHERE daily_record_id = :dailyRecordId")
    fun observeTasksForDailyRecord(dailyRecordId: String): Flow<List<TaskEntryEntity>>

    @Query("UPDATE task_entries SET is_completed = :isCompleted, completed_at = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: String, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE task_entries SET current_value = :currentValue WHERE id = :id")
    suspend fun updateTaskCurrentValue(id: String, currentValue: Double)

    @Query(
        "SELECT * FROM task_entries WHERE daily_record_id = (SELECT daily_record_id FROM task_entries WHERE id = :taskId) AND habit_type LIKE 'WORKOUT%'"
    )
    suspend fun getWorkoutTasksForSameDay(taskId: String): List<TaskEntryEntity>

    @Query("DELETE FROM task_entries")
    suspend fun clearAllTasks()
}
