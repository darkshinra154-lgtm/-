package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.TaskApplication
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.toInt())

        when (intent.action) {
            NotificationHelper.ACTION_MARK_DONE -> {
                val app = context.applicationContext as? TaskApplication
                if (app != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.repository.toggleTaskCompletion(taskId, true)
                        AlarmScheduler.cancelTaskReminder(context, taskId)
                    }
                }
                Toast.makeText(context, "تم إكمال المهمة بنجاح ✓", Toast.LENGTH_SHORT).show()
            }

            NotificationHelper.ACTION_SNOOZE -> {
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "مهمة"
                val description = intent.getStringExtra(NotificationHelper.EXTRA_TASK_DESC) ?: ""
                val priority = intent.getStringExtra(NotificationHelper.EXTRA_TASK_PRIORITY) ?: "MEDIUM"

                AlarmScheduler.scheduleSnooze(
                    context = context,
                    taskId = taskId,
                    title = title,
                    description = description,
                    priority = priority,
                    snoozeMinutes = 15
                )
                Toast.makeText(context, "تم تأجيل التنبيه 15 دقيقة ⏱", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
