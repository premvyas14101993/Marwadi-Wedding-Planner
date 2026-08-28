package com.example.ui.screens.rituals

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MaterialEntity
import com.example.data.local.entities.RitualChecklistItemEntity
import com.example.data.local.entities.RitualEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.RoyalCard
import com.example.ui.components.RoyalSectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualDetailScreen(
    ritualId: Long,
    viewModel: WeddingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rituals by viewModel.rituals.collectAsState()
    val allChecklists by viewModel.allChecklists.collectAsState()
    val allMaterials by viewModel.materials.collectAsState()
    val allExpenses by viewModel.expenses.collectAsState()

    val ritual = rituals.find { it.id == ritualId }
    val checklist = allChecklists.filter { it.ritualId == ritualId }
    val materials = allMaterials.filter { it.ritualId == ritualId }
    val expenses = allExpenses.filter { it.ritualId == ritualId || it.ritualName == ritual?.name }

    var showAddChecklistDialog by remember { mutableStateOf(false) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showEditRitualDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (ritual == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Ritual not found", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val completedChecklist = checklist.count { it.isCompleted }
    val checklistProgress = if (checklist.isNotEmpty()) completedChecklist.toFloat() / checklist.size else 0f

    val purchasedMaterials = materials.count { it.isPurchased }
    val totalEstimatedCost = materials.sumOf { it.estimatedCost }
    val totalExpenseForRitual = expenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ritual.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (ritual.hindiName.isNotEmpty()) {
                            Text(
                                text = ritual.hindiName,
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalGoldLight
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditRitualDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Ritual Details",
                            tint = RoyalGoldLight
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Ritual",
                            tint = Color(0xFFFFB4AB)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalMaroonDark)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status & Progress Header Card
            item {
                RoyalCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ritual Status",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusBadge(status = ritual.status)
                                    StatusBadge(status = ritual.priority)
                                }
                            }

                            // Quick Status Change Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        val next = when (ritual.status) {
                                            "PENDING" -> "IN_PROGRESS"
                                            "IN_PROGRESS" -> "COMPLETED"
                                            else -> "PENDING"
                                        }
                                        viewModel.updateRitual(ritual.copy(status = next))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (ritual.status) {
                                            "COMPLETED" -> SuccessGreen
                                            "IN_PROGRESS" -> InfoBlue
                                            else -> RoyalMaroon
                                        }
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when (ritual.status) {
                                            "COMPLETED" -> "Completed"
                                            "IN_PROGRESS" -> "In Progress"
                                            else -> "Mark Active"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = CardBorderGold)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Logistics Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Date & Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = if (ritual.date != null) "${formatDate(ritual.date)} ${ritual.time}" else "Date not set",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Venue / Place", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = ritual.venue?.ifEmpty { "Venue TBD" } ?: "Venue TBD",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!ritual.responsiblePerson.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = RoyalGoldDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Family Lead: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = ritual.responsiblePerson!!, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cultural Significance & Vidhi Details
            item {
                RoyalCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cultural Significance & Vidhi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ritual.culturalSignificance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (ritual.vidhiDetails.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Traditional Vidhi Steps:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalMaroon
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ritual.vidhiDetails,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Checklist Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Actionable Checklist ($completedChecklist/${checklist.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Check off tasks as family preparations progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showAddChecklistDialog = true },
                        modifier = Modifier.testTag("add_checklist_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = RoyalMaroon)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                MarwadiProgressBar(progress = checklistProgress, height = 6.dp)
            }

            if (checklist.isEmpty()) {
                item {
                    Text(
                        text = "No checklist items added. Tap '+' to create custom preparation tasks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(checklist, key = { "ritual_chk_${it.id}_${it.title.hashCode()}" }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isCompleted) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { viewModel.toggleChecklistItem(item) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SuccessGreen,
                                    uncheckedColor = RoyalMaroon
                                )
                            )
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (item.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.toggleChecklistItem(item) }
                            )
                            IconButton(onClick = { viewModel.deleteChecklistItem(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Item",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Samagri / Materials Required Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Required Samagri & Materials ($purchasedMaterials/${materials.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estimated Cost: ${formatCurrency(totalEstimatedCost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showAddMaterialDialog = true },
                        modifier = Modifier.testTag("add_material_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Samagri", tint = RoyalMaroon)
                    }
                }
            }

            if (materials.isEmpty()) {
                item {
                    Text(
                        text = "No samagri items listed yet. Tap '+' to add required puja materials or gifts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(materials, key = { "ritual_mat_${it.id}_${it.item.hashCode()}" }) { mat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mat.isPurchased) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = mat.isPurchased,
                                    onCheckedChange = { viewModel.toggleMaterialPurchased(mat) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SuccessGreen,
                                        uncheckedColor = RoyalMaroon
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = mat.item,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (mat.isPurchased) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "${mat.requiredQuantity} ${mat.unit} • ${mat.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (mat.estimatedCost > 0) {
                                    Text(
                                        text = formatCurrency(mat.estimatedCost),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalMaroon
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteMaterial(mat) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Linked Expenses Summary
            if (expenses.isNotEmpty()) {
                item {
                    RoyalSectionHeader(title = "Expenses for this Ritual")
                    RoyalCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Spent on ${ritual.name}", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = formatCurrency(totalExpenseForRitual),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroon
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            expenses.forEach { exp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "• ${exp.expenseName} (${exp.paidBy})", style = MaterialTheme.typography.bodySmall)
                                    Text(text = formatCurrency(exp.amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showAddChecklistDialog) {
        var checklistText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddChecklistDialog = false },
            title = { Text("Add Preparation Task", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = checklistText,
                    onValueChange = { checklistText = it },
                    label = { Text("Task / Checklist Item") },
                    placeholder = { Text("e.g. Arrange 5 brass thalis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (checklistText.isNotBlank()) {
                            viewModel.addChecklistItem(
                                RitualChecklistItemEntity(
                                    weddingId = ritual.weddingId,
                                    ritualId = ritual.id,
                                    title = checklistText.trim(),
                                    isCompleted = false
                                )
                            )
                            showAddChecklistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddChecklistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddMaterialDialog) {
        var itemName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Puja Samagri") }
        var quantityStr by remember { mutableStateOf("1") }
        var unit by remember { mutableStateOf("pcs") }
        var costStr by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddMaterialDialog = false },
            title = { Text("Add Ritual Material / Samagri", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Haldi powder, Nariyal, Chunri") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (kg/pcs/meters)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Estimated Cost (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            viewModel.addMaterial(
                                MaterialEntity(
                                    weddingId = ritual.weddingId,
                                    ritualId = ritual.id,
                                    ritualName = ritual.name,
                                    item = itemName.trim(),
                                    category = category,
                                    requiredQuantity = quantityStr.toDoubleOrNull() ?: 1.0,
                                    unit = unit.trim(),
                                    estimatedCost = costStr.toDoubleOrNull() ?: 0.0
                                )
                            )
                            showAddMaterialDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Add Samagri", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddMaterialDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditRitualDialog) {
        var venue by remember { mutableStateOf(ritual.venue ?: "") }
        var responsible by remember { mutableStateOf(ritual.responsiblePerson ?: "") }
        var time by remember { mutableStateOf(ritual.time ?: "10:00 AM") }
        var dateMillis by remember { mutableStateOf(ritual.date ?: System.currentTimeMillis()) }

        val cal = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day)
                dateMillis = c.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        AlertDialog(
            onDismissRequest = { showEditRitualDialog = false },
            title = { Text("Edit Ritual Logistics", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { datePicker.show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Date: ${formatDate(dateMillis)}")
                    }
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g. 10:00 AM / Shubh Muhurat)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue / Hall") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = responsible,
                        onValueChange = { responsible = it },
                        label = { Text("Responsible Family Member") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateRitual(
                            ritual.copy(
                                venue = venue,
                                responsiblePerson = responsible,
                                time = time,
                                date = dateMillis
                            )
                        )
                        showEditRitualDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditRitualDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Ritual?",
            message = "Are you sure you want to remove '${ritual.name}' (${ritual.hindiName})? All associated checklist tasks and samagri materials will also be permanently deleted.",
            confirmText = "Delete Ritual",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteRitual(ritual)
                showDeleteConfirm = false
                onBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
