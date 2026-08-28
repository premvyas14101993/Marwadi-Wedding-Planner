package com.example.ui.screens.rituals

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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RitualEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.RoyalCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun RitualListScreen(
    viewModel: WeddingViewModel,
    onRitualSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val rituals by viewModel.rituals.collectAsState()
    val allChecklists by viewModel.allChecklists.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, IN_PROGRESS, COMPLETED, HIGH_PRIORITY
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var ritualToDelete by remember { mutableStateOf<RitualEntity?>(null) }

    val filteredRituals = rituals.filter { r ->
        val matchesSearch = r.name.contains(searchQuery, ignoreCase = true) ||
                r.hindiName.contains(searchQuery, ignoreCase = true) ||
                r.description.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "PENDING" -> r.status == "PENDING"
            "IN_PROGRESS" -> r.status == "IN_PROGRESS"
            "COMPLETED" -> r.status == "COMPLETED"
            "HIGH_PRIORITY" -> r.priority == "HIGH"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    val completedCount = rituals.count { it.status == "COMPLETED" }
    val progress = if (rituals.isNotEmpty()) completedCount.toFloat() / rituals.size else 0f

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats Banner
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
                                    text = "26 Traditional Marwadi Rituals",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroonDark
                                )
                                Text(
                                    text = "$completedCount of ${rituals.size} Completed",
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
                    placeholder = { Text("Search ritual (e.g. Ganesh, Haldi, Mayra, Pheras)...") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_ritual_input")
                )
            }

            // Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${rituals.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "IN_PROGRESS",
                            onClick = { selectedFilter = "IN_PROGRESS" },
                            label = { Text("In Progress (${rituals.count { it.status == "IN_PROGRESS" }})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "PENDING",
                            onClick = { selectedFilter = "PENDING" },
                            label = { Text("Pending (${rituals.count { it.status == "PENDING" }})") }
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

            // Ritual Cards List
            if (filteredRituals.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Rituals Found",
                        message = "No rituals match your current search or filter criteria.",
                        icon = Icons.Default.Celebration
                    )
                }
            } else {
                items(filteredRituals, key = { it.id }) { ritual ->
                    val ritualChecklists = allChecklists.filter { it.ritualId == ritual.id }
                    val completedItems = ritualChecklists.count { it.isCompleted }
                    val totalItems = ritualChecklists.size

                    RitualListItemCard(
                        ritual = ritual,
                        checklistProgress = if (totalItems > 0) "$completedItems/$totalItems Tasks" else null,
                        onClick = { onRitualSelected(ritual.id) },
                        onDelete = { ritualToDelete = ritual },
                        onToggleStatus = {
                            val nextStatus = when (ritual.status) {
                                "PENDING" -> "IN_PROGRESS"
                                "IN_PROGRESS" -> "COMPLETED"
                                else -> "PENDING"
                            }
                            viewModel.updateRitual(ritual.copy(status = nextStatus))
                        }
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
                .testTag("add_custom_ritual_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ritual")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Ritual", fontWeight = FontWeight.Bold)
            }
        }
    }

    ritualToDelete?.let { ritual ->
        ConfirmDialog(
            title = "Delete Ritual?",
            message = "Are you sure you want to remove '${ritual.name}' (${ritual.hindiName})? All associated checklist tasks and samagri materials will also be removed.",
            confirmText = "Delete Ritual",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteRitual(ritual)
                ritualToDelete = null
            },
            onDismiss = { ritualToDelete = null }
        )
    }

    if (showAddDialog && currentWedding != null) {
        AddCustomRitualDialog(
            weddingId = currentWedding!!.id,
            onDismiss = { showAddDialog = false },
            onAdd = { newRitual ->
                viewModel.addRitual(newRitual)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RitualListItemCard(
    ritual: RitualEntity,
    checklistProgress: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderGold, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("ritual_card_${ritual.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (ritual.status) {
                                    "COMPLETED" -> SuccessGreen.copy(alpha = 0.15f)
                                    "IN_PROGRESS" -> InfoBlue.copy(alpha = 0.15f)
                                    else -> RoyalMaroon.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${ritual.orderIndex}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (ritual.status) {
                                "COMPLETED" -> SuccessGreen
                                "IN_PROGRESS" -> InfoBlue
                                else -> RoyalMaroon
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = ritual.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (ritual.hindiName.isNotEmpty()) {
                            Text(
                                text = ritual.hindiName,
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalGoldDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier.clickable { onToggleStatus() }
                    ) {
                        StatusBadge(status = ritual.status)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_ritual_${ritual.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Ritual",
                            tint = Color.Gray.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ritual.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (ritual.date != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = RoyalMaroon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatDate(ritual.date),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    if (checklistProgress != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = checklistProgress,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = RoyalMaroon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vidhi & Materials",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalMaroon,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomRitualDialog(
    weddingId: Long,
    onDismiss: () -> Unit,
    onAdd: (RitualEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hindiName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var responsiblePerson by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Wedding Ritual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ritual Name *") },
                    placeholder = { Text("e.g. Samdhi Milap, Shubh Lagan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hindiName,
                    onValueChange = { hindiName = it },
                    label = { Text("Hindi Name") },
                    placeholder = { Text("e.g. समधी मिलाप") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = responsiblePerson,
                    onValueChange = { responsiblePerson = it },
                    label = { Text("Responsible Person / Family Lead") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            RitualEntity(
                                weddingId = weddingId,
                                name = name,
                                hindiName = hindiName,
                                description = description,
                                responsiblePerson = responsiblePerson,
                                venue = venue,
                                priority = priority,
                                orderIndex = 99
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
            ) {
                Text("Add Ritual", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
