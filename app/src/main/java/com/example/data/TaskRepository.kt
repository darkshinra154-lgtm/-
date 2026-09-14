package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    fun getTasksForDateRange(startMillis: Long, endMillis: Long): Flow<List<TaskItem>> {
        return taskDao.getTasksForDateRange(startMillis, endMillis)
    }

    suspend fun getTaskById(id: Long): TaskItem? {
        return taskDao.getTaskById(id)
    }

    suspend fun getActiveTasksWithReminders(): List<TaskItem> {
        return taskDao.getActiveTasksWithReminders()
    }

    suspend fun insertTask(task: TaskItem): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    suspend fun toggleTaskCompletion(id: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(id, isCompleted, completedAt)
    }

    suspend fun updateReminderStatus(id: Long, hasReminder: Boolean) {
        taskDao.updateReminderStatus(id, hasReminder)
    }
}
