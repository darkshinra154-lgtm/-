package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TaskPriority(val titleAr: String, val level: Int) {
    HIGH("عالية", 3),
    MEDIUM("متوسطة", 2),
    LOW("عادية", 1)
}

enum class TaskCategory(val titleAr: String, val iconName: String) {
    WORK("عمل", "work"),
    PERSONAL("شخصي", "personal"),
    STUDY("دراسة", "study"),
    HEALTH("صحة", "health"),
    SHOPPING("تسوق", "shopping"),
    OTHER("أخرى", "other")
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dateMillis: Long, // Start of the day in millis
    val dueHour: Int = 9,
    val dueMinute: Int = 0,
    val hasReminder: Boolean = true,
    val reminderOffsetMinutes: Int = 0, // 0 = at time, 15 = 15m before, 30 = 30m before, 60 = 1h before
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val smartTip: String? = null
) {
    /**
     * Calculates the exact timestamp in milliseconds when the reminder should trigger.
     */
    fun calculateReminderTimeMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, dueHour)
            set(Calendar.MINUTE, dueMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis - (reminderOffsetMinutes * 60 * 1000L)
    }

    /**
     * Format time for display (e.g. "09:30 ص" or "02:15 م")
     */
    fun getFormattedTime(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, dueHour)
            set(Calendar.MINUTE, dueMinute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
        return sdf.format(calendar.time)
    }

    /**
     * Format date for display (e.g. "اليوم", "غداً", or "14 سبتمبر")
     */
    fun getFormattedDate(): String {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val taskCal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffDays = ((taskCal.timeInMillis - nowCal.timeInMillis) / (24 * 60 * 60 * 1000L)).toInt()
        return when (diffDays) {
            0 -> "اليوم"
            1 -> "غداً"
            -1 -> "أمس"
            else -> {
                val sdf = SimpleDateFormat("d MMMM", Locale("ar"))
                sdf.format(Date(dateMillis))
            }
        }
    }
}
