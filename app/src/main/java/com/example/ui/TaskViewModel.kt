package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.data.TaskRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TaskFilter(val titleAr: String) {
    TODAY("اليوم"),
    ALL("الكل"),
    UPCOMING("القادمة"),
    COMPLETED("المكتملة")
}

data class TaskUiState(
    val allTasks: List<TaskItem> = emptyList(),
    val filteredTasks: List<TaskItem> = emptyList(),
    val currentFilter: TaskFilter = TaskFilter.TODAY,
    val selectedCategory: TaskCategory? = null,
    val searchQuery: String = "",
    val selectedDateMillis: Long = getTodayStartMillis(),
    val totalTodayCount: Int = 0,
    val completedTodayCount: Int = 0,
    val highPriorityPendingCount: Int = 0,
    val smartDailyTip: String = "ركز على إنجاز المهام الأكثر أهمية أولاً لتحقيق أفضل إنتاجية",
    val isAddSheetOpen: Boolean = false,
    val editingTask: TaskItem? = null,
    val isSearchOpen: Boolean = false
)

private data class UiParams(
    val filter: TaskFilter = TaskFilter.TODAY,
    val category: TaskCategory? = null,
    val query: String = "",
    val isAddSheetOpen: Boolean = false,
    val editingTask: TaskItem? = null,
    val isSearchOpen: Boolean = false
)

fun getTodayStartMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val _uiParams = MutableStateFlow(UiParams())

    val uiState: StateFlow<TaskUiState> = combine(
        repository.allTasks,
        _uiParams
    ) { tasks, params ->
        val todayStart = getTodayStartMillis()
        val tomorrowStart = todayStart + (24 * 60 * 60 * 1000L)

        val todayTasks = tasks.filter { it.dateMillis in todayStart until tomorrowStart }
        val completedToday = todayTasks.count { it.isCompleted }
        val totalToday = todayTasks.size
        val highPriorityPending = tasks.count { !it.isCompleted && it.priority == TaskPriority.HIGH }

        val filtered = tasks.filter { task ->
            // Filter by Category
            val matchesCategory = params.category == null || task.category == params.category

            // Filter by Search Query
            val matchesQuery = params.query.isBlank() ||
                    task.title.contains(params.query, ignoreCase = true) ||
                    task.description.contains(params.query, ignoreCase = true)

            // Filter by Mode
            val matchesFilter = when (params.filter) {
                TaskFilter.TODAY -> task.dateMillis in todayStart until tomorrowStart
                TaskFilter.ALL -> true
                TaskFilter.UPCOMING -> task.dateMillis >= tomorrowStart && !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }

            matchesCategory && matchesQuery && matchesFilter
        }

        // Generate smart dynamic tip
        val smartTip = when {
            totalToday == 0 -> "يومك هادئ! أضف مهامك لترتيب يومك وتحفيز إنجازك."
            completedToday == totalToday -> "رائع جداً! أتممت جميع مهام اليوم بنجاح 🎉"
            completedToday > 0 -> "أحسنت الاستمرار! أنجزت $completedToday من أصل $totalToday مهام."
            highPriorityPending > 0 -> "لديك $highPriorityPending مهمة عالية الأهمية تستحق تركيزك الآن."
            else -> "تذكر: تنظيم المهام يقلل التشتت ويزيد راحة البال."
        }

        TaskUiState(
            allTasks = tasks,
            filteredTasks = filtered,
            currentFilter = params.filter,
            selectedCategory = params.category,
            searchQuery = params.query,
            selectedDateMillis = todayStart,
            totalTodayCount = totalToday,
            completedTodayCount = completedToday,
            highPriorityPendingCount = highPriorityPending,
            smartDailyTip = smartTip,
            isAddSheetOpen = params.isAddSheetOpen,
            editingTask = params.editingTask,
            isSearchOpen = params.isSearchOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun setFilter(filter: TaskFilter) {
        _uiParams.update { it.copy(filter = filter) }
    }

    fun setCategory(category: TaskCategory?) {
        _uiParams.update { it.copy(category = category) }
    }

    fun setSearchQuery(query: String) {
        _uiParams.update { it.copy(query = query) }
    }

    fun toggleSearch(open: Boolean) {
        _uiParams.update { it.copy(isSearchOpen = open, query = if (!open) "" else it.query) }
    }

    fun openAddSheet(taskToEdit: TaskItem? = null) {
        _uiParams.update { it.copy(editingTask = taskToEdit, isAddSheetOpen = true) }
    }

    fun closeAddSheet() {
        _uiParams.update { it.copy(editingTask = null, isAddSheetOpen = false) }
    }

    fun saveTask(
        id: Long = 0L,
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dateMillis: Long,
        dueHour: Int,
        dueMinute: Int,
        hasReminder: Boolean,
        reminderOffsetMinutes: Int
    ) {
        viewModelScope.launch {
            val smartTip = generateSmartTip(category, priority, dueHour)

            val task = TaskItem(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dateMillis = dateMillis,
                dueHour = dueHour,
                dueMinute = dueMinute,
                hasReminder = hasReminder,
                reminderOffsetMinutes = reminderOffsetMinutes,
                smartTip = smartTip
            )

            if (id == 0L) {
                val newId = repository.insertTask(task)
                val savedTask = task.copy(id = newId)
                if (hasReminder) {
                    AlarmScheduler.scheduleTaskReminder(getApplication(), savedTask)
                }
            } else {
                repository.updateTask(task)
                if (hasReminder) {
                    AlarmScheduler.scheduleTaskReminder(getApplication(), task)
                } else {
                    AlarmScheduler.cancelTaskReminder(getApplication(), id)
                }
            }
            closeAddSheet()
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.toggleTaskCompletion(task.id, newStatus)
            if (newStatus) {
                AlarmScheduler.cancelTaskReminder(getApplication(), task.id)
            } else if (task.hasReminder) {
                AlarmScheduler.scheduleTaskReminder(getApplication(), task)
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelTaskReminder(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }

    fun toggleTaskReminder(task: TaskItem) {
        viewModelScope.launch {
            val newReminderState = !task.hasReminder
            repository.updateReminderStatus(task.id, newReminderState)
            val updated = task.copy(hasReminder = newReminderState)
            if (newReminderState) {
                AlarmScheduler.scheduleTaskReminder(getApplication(), updated)
            } else {
                AlarmScheduler.cancelTaskReminder(getApplication(), task.id)
            }
        }
    }

    fun testReminder() {
        NotificationHelper.showImmediateTestNotification(getApplication())
    }

    private fun generateSmartTip(category: TaskCategory, priority: TaskPriority, hour: Int): String {
        return when {
            priority == TaskPriority.HIGH && hour < 12 -> "الصباح الباكر هو ذروة التركيز الذهني لإنجاز المهام الهامة."
            priority == TaskPriority.HIGH -> "مهمة عاجلة: جهّز بيئة هادئة بدون مشتتات قبل البدء بها."
            category == TaskCategory.HEALTH -> "العناية بصحتك وراحتك هي الأساس لكل إنجاز."
            category == TaskCategory.STUDY -> "قسّم فترات الدراسة لـ 25 دقيقة تركيز و5 دقائق راحة (طريقة بومودورو)."
            category == TaskCategory.WORK -> "اكتب خطوات العمل أولاً لتسريع الإنجاز بنصف الوقت."
            category == TaskCategory.SHOPPING -> "قائمة التسوق المكتوبة توفر وقتك وتقلل الشراء العشوائي."
            else -> "كل خطوة صغيرة تقربك من تحقيق أهدافك اليومية."
        }
    }
}

class TaskViewModelFactory(
    private val application: Application,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
