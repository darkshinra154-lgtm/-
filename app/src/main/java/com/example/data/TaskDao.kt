package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dateMillis ASC, dueHour ASC, dueMinute ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE dateMillis >= :startMillis AND dateMillis < :endMillis ORDER BY isCompleted ASC, dueHour ASC, dueMinute ASC")
    fun getTasksForDateRange(startMillis: Long, endMillis: Long): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskItem?

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND hasReminder = 1")
    suspend fun getActiveTasksWithReminders(): List<TaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, completed: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET hasReminder = :hasReminder WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, hasReminder: Boolean)
}
