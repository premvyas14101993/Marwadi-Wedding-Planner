package com.example.ui.screens.expenses

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entities.ExpenseEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoyalCard
import com.example.ui.components.RoyalSectionHeader
import com.example.ui.components.RoyalStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.BentoSlate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel
import java.io.File
import java.util.Calendar

val EXPENSE_CATEGORIES = listOf(
    "All Categories",
    "Venue & Accommodation",
    "Catering & Sweets",
    "Jewelry & Poshak",
    "Decoration & Mandap",
    "Photography & Video",
    "Music & Band / Dhol",
    "Puja & Ritual Samagri",
    "Gifts & Shagun / Envelopes",
    "Transportation & Logistics",
    "Makeup & Mehndi Artist",
    "Invitations & Printing",
    "Miscellaneous"
)

@Composable
fun ExpenseListScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val expenseByPerson by viewModel.expenseByPerson.collectAsState()
    val rituals by viewModel.rituals.collectAsState()
    val people by viewModel.people.collectAsState()

    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedPaidBy by remember { mutableStateOf("All Payers") }
    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToViewDetail by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var unauthorizedDeleteExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToViewProof by remember { mutableStateOf<ExpenseEntity?>(null) }

    val distinctPayers = listOf("All Payers") + expenses.map { it.paidBy }.distinct()

    val filteredExpenses = expenses.filter { exp ->
        val matchesCategory = selectedCategory == "All Categories" || exp.category == selectedCategory
        val matchesPaidBy = selectedPaidBy == "All Payers" || exp.paidBy == selectedPaidBy
        val matchesSearch = exp.expenseName.contains(searchQuery, ignoreCase = true) ||
                exp.paidBy.contains(searchQuery, ignoreCase = true) ||
                (exp.ritualName != null && exp.ritualName!!.contains(searchQuery, ignoreCase = true))

        matchesCategory && matchesPaidBy && matchesSearch
    }

    val overallBudget = currentWedding?.overallBudget ?: 0.0
    val remainingBudget = maxOf(0.0, overallBudget - totalExpense)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoyalStatCard(
                        title = "Total Spent",
                        value = formatCurrency(totalExpense),
                        subtitle = "${expenses.size} entries",
                        icon = Icons.Default.ReceiptLong,
                        iconTint = RoyalMaroon,
                        modifier = Modifier.weight(1f)
                    )
                    RoyalStatCard(
                        title = "Remaining",
                        value = formatCurrency(remainingBudget),
                        subtitle = "of ${formatCurrency(overallBudget)}",
                        icon = Icons.Default.CurrencyRupee,
                        iconTint = if (remainingBudget > 0) SuccessGreen else ErrorCrimson,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // "Paid By" Quick Breakdown Carousel
            if (expenseByPerson.isNotEmpty()) {
                item {
                    Text(
                        text = "Family Contributions (Paid By)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroonDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(expenseByPerson.entries.toList()) { entry ->
                            Surface(
                                color = if (selectedPaidBy == entry.key) RoyalMaroon else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                                modifier = Modifier.clickable {
                                    selectedPaidBy = if (selectedPaidBy == entry.key) "All Payers" else entry.key
                                }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedPaidBy == entry.key) RoyalGoldLight else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatCurrency(entry.value),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPaidBy == entry.key) Color.White else RoyalMaroon
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search expense, vendor, or payer...") },
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

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EXPENSE_CATEGORIES) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Expenses Found",
                        message = "Tap '+ Add Expense' to record wedding expenses with 'Paid By' tracking and upload receipts/UPI screenshots.",
                        icon = Icons.Default.ReceiptLong
                    )
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onViewDetail = { expenseToViewDetail = expense },
                        onEdit = { expenseToEdit = expense },
                        onDelete = {
                            if (viewModel.canDeleteExpense(expense)) {
                                expenseToDelete = expense
                            } else {
                                unauthorizedDeleteExpense = expense
                            }
                        },
                        onViewProof = { expenseToViewProof = expense }
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
                .testTag("add_expense_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Expense", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        ExpenseEditorDialog(
            weddingId = currentWedding!!.id,
            existingExpense = null,
            ritualsList = rituals.map { it.name },
            peopleList = people.map { it.name },
            onDismiss = { showAddDialog = false },
            onSave = { newExpense ->
                viewModel.addExpense(newExpense)
                showAddDialog = false
            }
        )
    }

    expenseToViewDetail?.let { exp ->
        ExpenseDetailViewDialog(
            expense = exp,
            onDismiss = { expenseToViewDetail = null },
            onEdit = {
                expenseToEdit = exp
                expenseToViewDetail = null
            },
            onDelete = {
                if (viewModel.canDeleteExpense(exp)) {
                    expenseToDelete = exp
                    expenseToViewDetail = null
                } else {
                    unauthorizedDeleteExpense = exp
                    expenseToViewDetail = null
                }
            },
            onViewProof = {
                expenseToViewProof = exp
            }
        )
    }

    expenseToEdit?.let { exp ->
        ExpenseEditorDialog(
            weddingId = exp.weddingId,
            existingExpense = exp,
            ritualsList = rituals.map { it.name },
            peopleList = people.map { it.name },
            onDismiss = { expenseToEdit = null },
            onSave = { updated ->
                viewModel.updateExpense(updated)
                expenseToEdit = null
            }
        )
    }

    expenseToDelete?.let { exp ->
        ConfirmDialog(
            title = "Delete Expense?",
            message = "Are you sure you want to delete '${exp.expenseName}' of ${formatCurrency(exp.amount)}?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteExpense(exp)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }

    unauthorizedDeleteExpense?.let { exp ->
        val author = exp.addedByName.ifBlank { exp.paidBy }
        val admin = currentWedding?.creatorName?.ifBlank { "Project Creator" } ?: "Project Creator"
        AlertDialog(
            onDismissRequest = { unauthorizedDeleteExpense = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ErrorCrimson.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ErrorCrimson,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Deletion Restricted",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This expense can only be deleted by the person who recorded it or by the Wedding Project Admin.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "• Added By: $author",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon
                            )
                            Text(
                                text = "• Wedding Admin: $admin",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalGoldDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { unauthorizedDeleteExpense = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got it", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    expenseToViewProof?.let { exp ->
        ExpenseReceiptViewerDialog(
            expense = exp,
            onDismiss = { expenseToViewProof = null }
        )
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewProof: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasProof = !expense.receiptUri.isNullOrBlank()
    val isPdf = expense.receiptMimeType?.contains("pdf", ignoreCase = true) == true ||
            expense.receiptFileName?.endsWith(".pdf", ignoreCase = true) == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .border(1.dp, CardBorderGold, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.expenseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatCurrency(expense.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroon
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paid By: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = expense.paidBy,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = RoyalGoldDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = expense.paymentMode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (expense.addedByName.isNotBlank() && !expense.addedByName.equals(expense.paidBy, ignoreCase = true)) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = RoyalGoldDark, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Added by: ${expense.addedByName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Proof of Expense / Receipt preview row
            if (hasProof) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPdf) InfoBlue.copy(alpha = 0.08f) else SuccessGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPdf) InfoBlue.copy(alpha = 0.3f) else SuccessGreen.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewProof() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!isPdf && expense.receiptUri != null) {
                                AsyncImage(
                                    model = File(expense.receiptUri),
                                    contentDescription = "Receipt Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(InfoBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "PDF Bill",
                                        tint = InfoBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = if (isPdf) InfoBlue else SuccessGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isPdf) "Bill PDF Attached" else "UPI / Receipt Proof",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPdf) InfoBlue else SuccessGreen
                                    )
                                }
                                Text(
                                    text = expense.receiptFileName ?: "Attached Proof",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon
                            )
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View Proof",
                                tint = RoyalMaroon,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.ritualName != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${expense.ritualName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoyalGoldDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(onClick = onViewDetail, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "View Details", tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailViewDialog(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewProof: () -> Unit
) {
    val context = LocalContext.current
    val hasProof = !expense.receiptUri.isNullOrBlank()
    val isPdf = expense.receiptMimeType?.contains("pdf", ignoreCase = true) == true ||
            expense.receiptFileName?.endsWith(".pdf", ignoreCase = true) == true

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = RoyalMaroon.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "EXPENSE DETAILS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expense Title
                Text(
                    text = expense.expenseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Category & Date Row
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = RoyalGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalGoldDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Huge Amount Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RoyalMaroon
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount Paid",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGoldLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(expense.amount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = expense.paymentMode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Attributes Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Paid By
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paid By", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = expense.paidBy,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon
                            )
                        }

                        // Added By (Recorded By)
                        if (expense.addedByName.isNotBlank()) {
                            HorizontalDivider(color = CardBorderGold.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = RoyalGoldDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recorded By", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = expense.addedByName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalMaroon
                                )
                            }
                        }

                        // Vendor / Payee
                        if (!expense.vendorName.isNullOrBlank()) {
                            HorizontalDivider(color = CardBorderGold.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Store, contentDescription = null, tint = RoyalGoldDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Vendor / Shop", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = expense.vendorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Linked Ritual
                        if (!expense.ritualName.isNullOrBlank()) {
                            HorizontalDivider(color = CardBorderGold.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Celebration, contentDescription = null, tint = SaffronOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Linked Ritual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = expense.ritualName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SaffronOrange
                                )
                            }
                        }

                        // Bill Number
                        if (expense.billNumber.isNotBlank()) {
                            HorizontalDivider(color = CardBorderGold.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bill / Receipt #", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = expense.billNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Notes / Remarks Section
                if (expense.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Notes & Remarks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = expense.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Attached Proof / Receipt Section
                if (hasProof) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Attached Proof / Bill",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPdf) InfoBlue.copy(alpha = 0.08f) else SuccessGreen.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPdf) InfoBlue.copy(alpha = 0.3f) else SuccessGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewProof() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (!isPdf && expense.receiptUri != null) {
                                    AsyncImage(
                                        model = File(expense.receiptUri),
                                        contentDescription = "Receipt Thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(InfoBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = "PDF Bill",
                                            tint = InfoBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = expense.receiptFileName ?: "Attached Proof Document",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isPdf) "Tap to view PDF invoice" else "Tap to view full receipt / screenshot",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isPdf) InfoBlue else SuccessGreen
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View",
                                tint = RoyalMaroon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ExpenseFileUtils.shareFile(
                                context = context,
                                filePath = expense.receiptUri ?: "",
                                expenseName = expense.expenseName,
                                amount = formatCurrency(expense.amount)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorCrimson, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = ErrorCrimson, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorDialog(
    weddingId: Long,
    existingExpense: ExpenseEntity?,
    ritualsList: List<String>,
    peopleList: List<String>,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingExpense?.expenseName ?: "") }
    var amountStr by remember { mutableStateOf(existingExpense?.amount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(existingExpense?.category ?: "Catering & Sweets") }
    var paidBy by remember { mutableStateOf(existingExpense?.paidBy ?: "Father of the Groom") }
    var paymentMode by remember { mutableStateOf(existingExpense?.paymentMode ?: "UPI") }
    var ritualName by remember { mutableStateOf(existingExpense?.ritualName ?: "") }
    var notes by remember { mutableStateOf(existingExpense?.notes ?: "") }
    var dateMillis by remember { mutableStateOf(existingExpense?.date ?: System.currentTimeMillis()) }

    // Proof / Attachment state
    var receiptUri by remember { mutableStateOf(existingExpense?.receiptUri) }
    var receiptFileName by remember { mutableStateOf(existingExpense?.receiptFileName) }
    var receiptMimeType by remember { mutableStateOf(existingExpense?.receiptMimeType) }

    // Launchers for Image and PDF
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val result = ExpenseFileUtils.copyUriToLocalStorage(context, it)
            if (result != null) {
                receiptUri = result.filePath
                receiptFileName = result.fileName
                receiptMimeType = result.mimeType
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val result = ExpenseFileUtils.copyUriToLocalStorage(context, it)
            if (result != null) {
                receiptUri = result.filePath
                receiptFileName = result.fileName
                receiptMimeType = result.mimeType
            }
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }

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

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingExpense == null) "Add Wedding Expense" else "Edit Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Title *") },
                    placeholder = { Text("e.g. Haldi Mithai & Dry Fruits") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        EXPENSE_CATEGORIES.drop(1).forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Paid By Selector
                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("Paid By (Family Member / Person) *") },
                    placeholder = { Text("e.g. Groom's Father, Mama Ji, Self") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Payment Mode
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = { paymentMode = it },
                        label = { Text("Payment Mode") },
                        placeholder = { Text("UPI/Cash/Cheque/Card") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = { datePicker.show() },
                        modifier = Modifier.weight(1f).padding(top = 8.dp)
                    ) {
                        Text(text = formatDate(dateMillis), style = MaterialTheme.typography.labelSmall)
                    }
                }

                OutlinedTextField(
                    value = ritualName,
                    onValueChange = { ritualName = it },
                    label = { Text("Associated Ritual (Optional)") },
                    placeholder = { Text("e.g. Mehendi, Mayra, Pheras") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Proof Upload Section
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = RoyalMaroon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Proof of Expense / Bill",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroonDark
                                )
                            }

                            if (receiptUri != null) {
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Attached",
                                        color = SuccessGreen,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (receiptUri != null) {
                            val isPdf = receiptMimeType?.contains("pdf", ignoreCase = true) == true ||
                                    receiptFileName?.endsWith(".pdf", ignoreCase = true) == true

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, CardBorderGold, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (!isPdf && receiptUri != null) {
                                        AsyncImage(
                                            model = File(receiptUri!!),
                                            contentDescription = "Receipt",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(InfoBlue.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = InfoBlue,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = receiptFileName ?: "Attached proof file",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isPdf) "PDF Document / Bill" else "UPI / Receipt Image",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        receiptUri = null
                                        receiptFileName = null
                                        receiptMimeType = null
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove file",
                                        tint = ErrorCrimson,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Photo", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change PDF", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            Text(
                                text = "Upload UPI screenshot, physical receipt photo, or vendor invoice PDF for record-keeping.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalMaroon.copy(alpha = 0.12f),
                                        contentColor = RoyalMaroon
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receipt / UPI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { pdfPickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = InfoBlue.copy(alpha = 0.12f),
                                        contentColor = InfoBlue
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bill / PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Bill Details") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            ExpenseEntity(
                                id = existingExpense?.id ?: 0,
                                weddingId = weddingId,
                                expenseName = name.trim(),
                                amount = amt,
                                date = dateMillis,
                                category = category,
                                paidBy = paidBy.trim().ifEmpty { "Groom's Family" },
                                paymentMode = paymentMode.trim().ifEmpty { "UPI" },
                                ritualName = ritualName.trim().ifEmpty { null },
                                receiptUri = receiptUri,
                                receiptFileName = receiptFileName,
                                receiptMimeType = receiptMimeType,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_expense_btn")
            ) {
                Text("Save Expense", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExpenseReceiptViewerDialog(
    expense: ExpenseEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val filePath = expense.receiptUri ?: return
    val fileName = expense.receiptFileName ?: "Expense Proof"
    val isPdf = expense.receiptMimeType?.contains("pdf", ignoreCase = true) == true ||
            fileName.endsWith(".pdf", ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = expense.expenseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroonDark
                        )
                        Text(
                            text = "${formatCurrency(expense.amount)} • Paid by ${expense.paidBy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content View (Image or PDF)
                if (!isPdf) {
                    val file = File(filePath)
                    if (file.exists()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 340.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CardBorderGold, RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.03f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Receipt Proof",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Proof file is not found on local storage.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Surface(
                        color = InfoBlue.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InfoBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "PDF Bill",
                                tint = InfoBlue,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PDF Document / Official Vendor Invoice",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Open External App & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ExpenseFileUtils.openFileWithExternalApp(
                                context = context,
                                filePath = filePath,
                                mimeType = expense.receiptMimeType
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open File", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            ExpenseFileUtils.shareFile(
                                context = context,
                                filePath = filePath,
                                expenseName = expense.expenseName,
                                amount = formatCurrency(expense.amount)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Proof", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

