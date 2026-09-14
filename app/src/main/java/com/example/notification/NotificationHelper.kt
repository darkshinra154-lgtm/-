package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.TaskActionReceiver

object NotificationHelper {

    const val CHANNEL_REMINDERS_ID = "channel_task_reminders"
    const val CHANNEL_REMINDERS_NAME = "تنبيهات المهام"
    const val CHANNEL_REMINDERS_DESC = "إشعارات تذكير بمواعيد المهام اليومية"

    const val ACTION_MARK_DONE = "com.example.ACTION_MARK_DONE"
    const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_DESC = "extra_task_desc"
    const val EXTRA_TASK_PRIORITY = "extra_task_priority"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_REMINDERS_DESC
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun showTaskReminderNotification(
        context: Context,
        taskId: Long,
        title: String,
        description: String,
        priority: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark as Done
        val markDoneIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 1).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 15 minutes
        val snoozeIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_DESC, description)
            putExtra(EXTRA_TASK_PRIORITY, priority)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val priorityBadge = when (priority) {
            "HIGH" -> "🚨 أولوية عالية"
            "MEDIUM" -> "⭐ أولوية متوسطة"
            else -> "📌 مهمة"
        }

        val contentText = if (description.isNotBlank()) {
            "$priorityBadge • $description"
        } else {
            "$priorityBadge • حان موعد إنجاز المهمة"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ تذكير: $title")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "✓ تم الإنجاز", markDonePendingIntent)
            .addAction(0, "⏱ تأجيل 15 دقيقة", snoozePendingIntent)

        notificationManager.notify(taskId.toInt(), builder.build())
    }

    fun showImmediateTestNotification(context: Context) {
        showTaskReminderNotification(
            context = context,
            taskId = 999999L,
            title = "تنبيه تجريبي ذكي",
            description = "نظام الإشعارات الذكية يعمل بنجاح! ستصلك التنبيهات في أوقاتها المحددة.",
            priority = "HIGH"
        )
    }
}
