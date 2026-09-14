package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority = try {
        TaskPriority.valueOf(value)
    } catch (e: Exception) {
        TaskPriority.MEDIUM
    }

    @TypeConverter
    fun fromCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): TaskCategory = try {
        TaskCategory.valueOf(value)
    } catch (e: Exception) {
        TaskCategory.PERSONAL
    }
}
