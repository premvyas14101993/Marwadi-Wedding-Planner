package com.example.ui.screens.tasks

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.TaskEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.RoyalStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel
import java.util.Calendar

@Composable
fun TaskManagementScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, IN_PROGRESS, COMPLETED, HIGH_PRIORITY
    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val completedCount = tasks.count { it.status == "COMPLETED" }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f
    val highPriorityCount = tasks.count { it.priority == "HIGH" && it.status != "COMPLETED" }

    val filteredTasks = tasks.filter { t ->
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> t.status == "PENDING"
            "IN_PROGRESS" -> t.status == "IN_PROGRESS"
            "COMPLETED" -> t.status == "COMPLETED"
            "HIGH_PRIORITY" -> t.priority == "HIGH"
            else -> true
        }
        val matchesSearch = t.taskName.contains(searchQuery, ignoreCase = true) ||
                t.assignedTo.contains(searchQuery, ignoreCase = true) ||
                t.category.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Header
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Wedding Action Tasks",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroonDark
                                )
                                Text(
                                    text = "$completedCount of ${tasks.size} Completed • $highPriorityCount High Priority",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = RoyalMaroon,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        MarwadiProgressBar(progress = progress, height = 8.dp)
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search task or assigned family member...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalMaroon) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${tasks.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "PENDING",
                            onClick = { selectedFilter = "PENDING" },
                            label = { Text("Pending (${tasks.count { it.status == "PENDING" }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "IN_PROGRESS",
                            onClick = { selectedFilter = "IN_PROGRESS" },
                            label = { Text("In Progress") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "COMPLETED",
                            onClick = { selectedFilter = "COMPLETED" },
                            label = { Text("Completed ($completedCount)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "HIGH_PRIORITY",
                            onClick = { selectedFilter = "HIGH_PRIORITY" },
                            label = { Text("High Priority") }
                        )
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Tasks Found",
                        message = "Tap '+ Add Task' to assign wedding responsibilities to family members.",
                        icon = Icons.Default.Assignment
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onToggle = { viewModel.toggleTaskStatus(task) },
                        onEdit = { taskToEdit = task },
                        onDelete = { taskToDelete = task }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = RoyalMaroon,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_task_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Task", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        TaskEditorDialog(
            weddingId = currentWedding!!.id,
            existingTask = null,
            onDismiss = { showAddDialog = false },
            onSave = { newTask ->
                viewModel.addTask(newTask)
                showAddDialog = false
            }
        )
    }

    taskToEdit?.let { t ->
        TaskEditorDialog(
            weddingId = t.weddingId,
            existingTask = t,
            onDismiss = { taskToEdit = null },
            onSave = { updated ->
                viewModel.updateTask(updated)
                taskToEdit = null
            }
        )
    }

    taskToDelete?.let { t ->
        ConfirmDialog(
            title = "Delete Task?",
            message = "Are you sure you want to delete '${t.taskName}'?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTask(t)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == "COMPLETED"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderGold, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SuccessGreen,
                        uncheckedColor = RoyalMaroon
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.taskName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "${task.category} • Assigned to: ${task.assignedTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = task.priority)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${formatDate(task.dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalMaroon, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TaskEditorDialog(
    weddingId: Long,
    existingTask: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingTask?.taskName ?: "") }
    var category by remember { mutableStateOf(existingTask?.category ?: "Logistics") }
    var assignedTo by remember { mutableStateOf(existingTask?.assignedTo ?: "Family Lead") }
    var priority by remember { mutableStateOf(existingTask?.priority ?: "MEDIUM") }
    var dueDateMillis by remember { mutableStateOf(existingTask?.dueDate ?: (System.currentTimeMillis() + (7L * 86400000L))) }

    val cal = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val c = Calendar.getInstance()
            c.set(year, month, day)
            dueDateMillis = c.timeInMillis
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingTask == null) "Add Action Task" else "Edit Task",
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task Description *") },
                    placeholder = { Text("e.g. Order Safas and Turbans") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_name_input")
                )
                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Assigned Person *") },
                    placeholder = { Text("e.g. Bhabhi, Mama Ji, Cousin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Logistics, Catering, Rituals") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { datePicker.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Due Date: ${formatDate(dueDateMillis)}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            TaskEntity(
                                id = existingTask?.id ?: 0,
                                weddingId = weddingId,
                                taskName = name.trim(),
                                category = category.trim(),
                                assignedTo = assignedTo.trim().ifEmpty { "Family Lead" },
                                priority = priority,
                                dueDate = dueDateMillis,
                                status = existingTask?.status ?: "PENDING"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text("Save Task", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
