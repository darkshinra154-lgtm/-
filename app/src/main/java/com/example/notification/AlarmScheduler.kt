package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.TaskItem
import com.example.receiver.TaskAlarmReceiver

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleTaskReminder(context: Context, task: TaskItem) {
        if (!task.hasReminder || task.isCompleted) {
            cancelTaskReminder(context, task.id)
            return
        }

        val triggerTime = task.calculateReminderTimeMillis()
        val now = System.currentTimeMillis()

        // Don't schedule past reminders
        if (triggerTime <= now) {
            Log.d(TAG, "Task reminder time is in the past for task ${task.id}, skipping.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
            putExtra(NotificationHelper.EXTRA_TASK_TITLE, task.title)
            putExtra(NotificationHelper.EXTRA_TASK_DESC, task.description)
            putExtra(NotificationHelper.EXTRA_TASK_PRIORITY, task.priority.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d(TAG, "Scheduled reminder for task ${task.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback scheduling: ${ex.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception scheduling alarm: ${e.message}")
        }
    }

    fun scheduleSnooze(
        context: Context,
        taskId: Long,
        title: String,
        description: String,
        priority: String,
        snoozeMinutes: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_TASK_ID, taskId)
            putExtra(NotificationHelper.EXTRA_TASK_TITLE, title)
            putExtra(NotificationHelper.EXTRA_TASK_DESC, description)
            putExtra(NotificationHelper.EXTRA_TASK_PRIORITY, priority)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
