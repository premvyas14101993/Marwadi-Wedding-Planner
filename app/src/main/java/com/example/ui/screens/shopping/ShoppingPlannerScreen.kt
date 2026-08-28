package com.example.ui.screens.shopping

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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MaterialEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.RoyalCard
import com.example.ui.components.RoyalStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.ConsolidatedShoppingItem
import com.example.ui.viewmodel.WeddingViewModel

val SHOPPING_CATEGORIES = listOf(
    "All Categories",
    "Puja Samagri",
    "Poshak & Attire",
    "Gifts & Shagun",
    "Sweets & Dry Fruits",
    "Decor & Floral",
    "Utensils & Thalis"
)

@Composable
fun ShoppingPlannerScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val materials by viewModel.materials.collectAsState()
    val consolidatedItems by viewModel.consolidatedShopping.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var selectedTab by remember { mutableStateOf("CONSOLIDATED") } // CONSOLIDATED, BY_RITUAL
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, PURCHASED
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val totalItems = consolidatedItems.size
    val completedItems = consolidatedItems.count { it.isFullyPurchased }
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    val totalEstCost = consolidatedItems.sumOf { it.estimatedTotalCost }

    val filteredConsolidated = consolidatedItems.filter { item ->
        val matchesCategory = selectedCategory == "All Categories" || item.category.contains(selectedCategory, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "PENDING" -> !item.isFullyPurchased
            "PURCHASED" -> item.isFullyPurchased
            else -> true
        }
        val matchesSearch = item.item.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesFilter && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Header Banner
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
                                    text = "Consolidated Shopping & Samagri",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroonDark
                                )
                                Text(
                                    text = "$completedItems of $totalItems Items Purchased • Est. Cost: ${formatCurrency(totalEstCost)}",
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
                    placeholder = { Text("Search shopping samagri (e.g. Haldi, Supari, Chunri)...") },
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
                            label = { Text("All ($totalItems)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "PENDING",
                            onClick = { selectedFilter = "PENDING" },
                            label = { Text("To Buy (${totalItems - completedItems})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "PURCHASED",
                            onClick = { selectedFilter = "PURCHASED" },
                            label = { Text("Purchased ($completedItems)") }
                        )
                    }
                }
            }

            // Shopping Items List
            if (filteredConsolidated.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Shopping Items",
                        message = "All items have been purchased or no items match your search.",
                        icon = Icons.Default.ShoppingCart
                    )
                }
            } else {
                items(filteredConsolidated, key = { it.item }) { item ->
                    ConsolidatedShoppingCard(
                        item = item,
                        onToggleAll = {
                            // Find all matching materials and toggle
                            val matching = materials.filter { it.item.trim().equals(item.item.trim(), ignoreCase = true) }
                            val makePurchased = !item.isFullyPurchased
                            matching.forEach { mat ->
                                viewModel.updateMaterial(
                                    mat.copy(
                                        isPurchased = makePurchased,
                                        purchasedQuantity = if (makePurchased) mat.requiredQuantity else 0.0
                                    )
                                )
                            }
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
                .testTag("add_shopping_item_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Item", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        AddShoppingItemDialog(
            weddingId = currentWedding!!.id,
            onDismiss = { showAddDialog = false },
            onAdd = { newMaterial ->
                viewModel.addMaterial(newMaterial)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ConsolidatedShoppingCard(
    item: ConsolidatedShoppingItem,
    onToggleAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderGold, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isFullyPurchased) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
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
                    Checkbox(
                        checked = item.isFullyPurchased,
                        onCheckedChange = { onToggleAll() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SuccessGreen,
                            uncheckedColor = RoyalMaroon
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.item,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (item.isFullyPurchased) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total: ${item.totalRequired} ${item.unit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroon
                    )
                    if (item.estimatedTotalCost > 0) {
                        Text(
                            text = formatCurrency(item.estimatedTotalCost),
                            style = MaterialTheme.typography.bodySmall,
                            color = RoyalGoldDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (item.ritualBreakdowns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "Needed across rituals:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        item.ritualBreakdowns.forEach { breakdown ->
                            Text(
                                text = "• $breakdown",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddShoppingItemDialog(
    weddingId: Long,
    onDismiss: () -> Unit,
    onAdd: (MaterialEntity) -> Unit
) {
    var item by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Puja Samagri") }
    var quantityStr by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pcs") }
    var costStr by remember { mutableStateOf("0") }
    var ritualName by remember { mutableStateOf("General Wedding Preparation") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Shopping Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item,
                    onValueChange = { item = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Supari, Haldi Gath, Poshak") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
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
                        label = { Text("Unit") },
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
                    if (item.isNotBlank()) {
                        onAdd(
                            MaterialEntity(
                                weddingId = weddingId,
                                item = item.trim(),
                                category = category.trim(),
                                requiredQuantity = quantityStr.toDoubleOrNull() ?: 1.0,
                                unit = unit.trim(),
                                estimatedCost = costStr.toDoubleOrNull() ?: 0.0,
                                ritualName = ritualName
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
            ) {
                Text("Add Item", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
