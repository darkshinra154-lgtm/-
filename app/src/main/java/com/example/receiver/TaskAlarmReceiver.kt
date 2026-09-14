package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.NotificationHelper

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val title = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "تذكير بمهمة"
        val description = intent.getStringExtra(NotificationHelper.EXTRA_TASK_DESC) ?: ""
        val priority = intent.getStringExtra(NotificationHelper.EXTRA_TASK_PRIORITY) ?: "MEDIUM"

        NotificationHelper.showTaskReminderNotification(
            context = context,
            taskId = taskId,
            title = title,
            description = description,
            priority = priority
        )
    }
}
