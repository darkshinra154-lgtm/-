package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.ui.getTodayStartMillis
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TaskTemplate(
    val title: String,
    val description: String,
    val category: TaskCategory,
    val priority: TaskPriority,
    val hour: Int,
    val minute: Int
)

val smartTemplates = listOf(
    TaskTemplate("تمرين رياضي ومشي", "30 دقيقة لياقة بدنية وتجديد النشاط", TaskCategory.HEALTH, TaskPriority.MEDIUM, 7, 0),
    TaskTemplate("مراجعة أهم أولويات العمل", "تحديد أهم المهام وإعداد جدول اليوم", TaskCategory.WORK, TaskPriority.HIGH, 9, 0),
    TaskTemplate("شرب الماء والاستراحة", "ترطيب الجسم وأخذ استراحة قصيرة", TaskCategory.HEALTH, TaskPriority.LOW, 11, 30),
    TaskTemplate("قراءة ورد يومي أو كتاب", "قراءة مفيدة لتطوير الذات", TaskCategory.STUDY, TaskPriority.MEDIUM, 20, 0),
    TaskTemplate("شراء مستلزمات المنزل", "شراء الخضار والمستلزمات الضرورية", TaskCategory.SHOPPING, TaskPriority.LOW, 17, 30)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    editingTask: TaskItem?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dateMillis: Long,
        dueHour: Int,
        dueMinute: Int,
        hasReminder: Boolean,
        reminderOffsetMinutes: Int
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var title by remember { mutableStateOf(editingTask?.title ?: "") }
    var description by remember { mutableStateOf(editingTask?.description ?: "") }
    var category by remember { mutableStateOf(editingTask?.category ?: TaskCategory.PERSONAL) }
    var priority by remember { mutableStateOf(editingTask?.priority ?: TaskPriority.MEDIUM) }
    var dateMillis by remember { mutableLongStateOf(editingTask?.dateMillis ?: getTodayStartMillis()) }
    var dueHour by remember { mutableIntStateOf(editingTask?.dueHour ?: 9) }
    var dueMinute by remember { mutableIntStateOf(editingTask?.dueMinute ?: 0) }
    var hasReminder by remember { mutableStateOf(editingTask?.hasReminder ?: true) }
    var reminderOffsetMinutes by remember { mutableIntStateOf(editingTask?.reminderOffsetMinutes ?: 0) }

    var isTitleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_task_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (editingTask == null) "إضافة مهمة جديدة" else "تعديل المهمة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Smart Suggestions (Only shown when creating a new task)
            if (editingTask == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "اقتراحات ذكية سريعة:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    smartTemplates.forEach { template ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    title = template.title
                                    description = template.description
                                    category = template.category
                                    priority = template.priority
                                    dueHour = template.hour
                                    dueMinute = template.minute
                                    isTitleError = false
                                }
                        ) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isTitleError = false
                },
                label = { Text("عنوان المهمة *") },
                isError = isTitleError,
                supportingText = {
                    if (isTitleError) {
                        Text("يرجى إدخال عنوان المهمة", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("تفاصيل أو ملاحظات إضافية (اختياري)") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_desc_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(
                text = "القسم / التصنيف:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskCategory.values().forEach { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat.titleAr) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Selection
            Text(
                text = "مستوى الأولوية:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { prio ->
                    val isSelected = priority == prio
                    val prioColor = when (prio) {
                        TaskPriority.HIGH -> PriorityHigh
                        TaskPriority.MEDIUM -> PriorityMedium
                        TaskPriority.LOW -> PriorityLow
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { priority = prio },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) prioColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, prioColor) else null
                    ) {
                        Text(
                            text = prio.titleAr,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) prioColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time & Date Pickers
            Text(
                text = "موعد الإنجاز:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Time Picker Button
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                dueHour = hour
                                dueMinute = minute
                            },
                            dueHour,
                            dueMinute,
                            false
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, dueHour)
                        set(Calendar.MINUTE, dueMinute)
                    }
                    val timeStr = SimpleDateFormat("hh:mm a", Locale("ar")).format(cal.time)
                    Text(text = timeStr, fontSize = 13.sp)
                }

                // Date Quick Selector (Today / Tomorrow)
                val todayStart = getTodayStartMillis()
                val tomorrowStart = todayStart + (24 * 60 * 60 * 1000L)
                val isToday = dateMillis == todayStart

                OutlinedButton(
                    onClick = {
                        dateMillis = if (isToday) tomorrowStart else todayStart
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isToday) "اليوم" else "غداً",
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Reminder Switch & Offset
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (hasReminder) MaterialTheme.colorScheme.primary else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "تنبيه ذكي للمهمة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "إشعار تفاعلي مع إمكانية التأجيل والإنجاز",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldGreen),
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }

                    // Offset Options
                    if (hasReminder) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "موعد الإشعار:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val offsets = listOf(
                                0 to "في الموعد",
                                15 to "قبل 15د",
                                30 to "قبل 30د",
                                60 to "قبل ساعة"
                            )
                            offsets.forEach { (mins, label) ->
                                val isSelected = reminderOffsetMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { reminderOffsetMinutes = mins },
                                    label = { Text(label, fontSize = 11.5.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save and Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("إلغاء", fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            isTitleError = true
                        } else {
                            onSave(
                                editingTask?.id ?: 0L,
                                title,
                                description,
                                category,
                                priority,
                                dateMillis,
                                dueHour,
                                dueMinute,
                                hasReminder,
                                reminderOffsetMinutes
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("save_task_button")
                ) {
                    Text(
                        text = if (editingTask == null) "إضافة المهمة" else "حفظ التعديلات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
