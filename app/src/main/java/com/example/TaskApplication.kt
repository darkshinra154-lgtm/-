package com.example

import android.app.Application
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TaskApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { TaskDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TaskRepository(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)
    }
}
