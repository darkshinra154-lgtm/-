package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [TaskItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "tasks_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate with a few helpful initial tasks in Arabic
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database.taskDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(taskDao: TaskDao) {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val sampleTasks = listOf(
                TaskItem(
                    title = "مراجعة خطة اليوم والأهداف",
                    description = "تنظيم أولويات العمل وتحديد أهم 3 مهام للإنجاز",
                    category = TaskCategory.WORK,
                    priority = TaskPriority.HIGH,
                    dateMillis = todayStart,
                    dueHour = 9,
                    dueMinute = 0,
                    hasReminder = true,
                    reminderOffsetMinutes = 0,
                    isCompleted = false,
                    smartTip = "البدء بالمهام ذات الأولوية العالية يرفع الإنتاجية بنسبة 40%"
                ),
                TaskItem(
                    title = "شرب الماء وممارسة تمارين التمدد",
                    description = "أخذ استراحة قصيرة لتجديد النشاط والتركيز",
                    category = TaskCategory.HEALTH,
                    priority = TaskPriority.MEDIUM,
                    dateMillis = todayStart,
                    dueHour = 11,
                    dueMinute = 30,
                    hasReminder = true,
                    reminderOffsetMinutes = 0,
                    isCompleted = false,
                    smartTip = "شرب كوب ماء كل ساعتين يحافظ على طاقتك الذهنية"
                ),
                TaskItem(
                    title = "قراءة 15 صفحة من الكتاب",
                    description = "تطوير الذات وتثبيت عادة القراءة اليومية",
                    category = TaskCategory.STUDY,
                    priority = TaskPriority.LOW,
                    dateMillis = todayStart,
                    dueHour = 20,
                    dueMinute = 0,
                    hasReminder = true,
                    reminderOffsetMinutes = 15,
                    isCompleted = false,
                    smartTip = "القراءة قبل النوم تساعد على تحسين جودة الاسترخاء"
                )
            )

            sampleTasks.forEach { task ->
                taskDao.insertTask(task)
            }
        }
    }
}
