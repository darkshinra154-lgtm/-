package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.ui.TaskFilter

@Composable
fun FilterAndCategoryRow(
    currentFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main Filters: Segmented Button Row
        val filters = TaskFilter.values()
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("filter_segmented_row")
        ) {
            filters.forEachIndexed { index, filter ->
                SegmentedButton(
                    selected = currentFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size),
                    modifier = Modifier.testTag("filter_${filter.name}")
                ) {
                    Text(
                        text = filter.titleAr,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (currentFilter == filter) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }

        // Categories Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("جميع الأقسام", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("category_all")
            )

            TaskCategory.values().forEach { category ->
                val isSelected = selectedCategory == category
                val icon = getCategoryIcon(category)

                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.titleAr, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("category_${category.name}")
                )
            }
        }
    }
}

fun getCategoryIcon(category: TaskCategory): ImageVector {
    return when (category) {
        TaskCategory.WORK -> Icons.Default.Work
        TaskCategory.PERSONAL -> Icons.Default.Person
        TaskCategory.STUDY -> Icons.Default.Book
        TaskCategory.HEALTH -> Icons.Default.Favorite
        TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
        TaskCategory.OTHER -> Icons.Default.Category
    }
}
