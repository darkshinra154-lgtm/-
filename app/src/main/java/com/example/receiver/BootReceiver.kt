package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.TaskApplication
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? TaskApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val activeTasks = app.repository.getActiveTasksWithReminders()
                activeTasks.forEach { task ->
                    AlarmScheduler.scheduleTaskReminder(context, task)
                }
            }
        }
    }
}
