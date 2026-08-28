package com.example.ui.screens.gifts

import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.GiftEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoyalStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun GiftManagementScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val gifts by viewModel.gifts.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, RETURNED, PENDING_RETURN
    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var giftToEdit by remember { mutableStateOf<GiftEntity?>(null) }
    var giftToDelete by remember { mutableStateOf<GiftEntity?>(null) }

    val totalShagunAmount = gifts.sumOf { it.estimatedValue }
    val returnGiftsGivenCount = gifts.count { it.returnGiftGiven.isNotBlank() }

    val filteredGifts = gifts.filter { g ->
        val matchesFilter = when (selectedFilter) {
            "RETURNED" -> g.returnGiftGiven.isNotBlank()
            "PENDING_RETURN" -> g.returnGiftGiven.isBlank()
            else -> true
        }
        val matchesSearch = g.giftItem.contains(searchQuery, ignoreCase = true) ||
                g.giverName.contains(searchQuery, ignoreCase = true) ||
                g.relationship.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoyalStatCard(
                        title = "Received Shagun",
                        value = formatCurrency(totalShagunAmount),
                        subtitle = "${gifts.size} Shagun Entries",
                        icon = Icons.Default.CardGiftcard,
                        iconTint = RoyalMaroon,
                        modifier = Modifier.weight(1f)
                    )
                    RoyalStatCard(
                        title = "Return Gifts Given",
                        value = "$returnGiftsGivenCount of ${gifts.size}",
                        subtitle = "Gifts reciprocated",
                        icon = Icons.Default.CardGiftcard,
                        iconTint = SaffronOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by giver name, relationship or gift...") },
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
                            label = { Text("All Shagun (${gifts.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "RETURNED",
                            onClick = { selectedFilter = "RETURNED" },
                            label = { Text("Return Gift Given ($returnGiftsGivenCount)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "PENDING_RETURN",
                            onClick = { selectedFilter = "PENDING_RETURN" },
                            label = { Text("Pending Return Gift (${gifts.size - returnGiftsGivenCount})") }
                        )
                    }
                }
            }

            if (filteredGifts.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Gifts Recorded",
                        message = "Tap '+ Add Shagun' to record received wedding envelopes, silver coins, or return gifts.",
                        icon = Icons.Default.CardGiftcard
                    )
                }
            } else {
                items(filteredGifts, key = { it.id }) { gift ->
                    GiftItemCard(
                        gift = gift,
                        onEdit = { giftToEdit = gift },
                        onDelete = { giftToDelete = gift }
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
                .testTag("add_gift_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Gift")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Shagun", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        GiftEditorDialog(
            weddingId = currentWedding!!.id,
            existingGift = null,
            onDismiss = { showAddDialog = false },
            onSave = { newGift ->
                viewModel.addGift(newGift)
                showAddDialog = false
            }
        )
    }

    giftToEdit?.let { g ->
        GiftEditorDialog(
            weddingId = g.weddingId,
            existingGift = g,
            onDismiss = { giftToEdit = null },
            onSave = { updated ->
                viewModel.updateGift(updated)
                giftToEdit = null
            }
        )
    }

    giftToDelete?.let { g ->
        ConfirmDialog(
            title = "Delete Gift Entry?",
            message = "Are you sure you want to delete '${g.giftItem}' from '${g.giverName}'?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteGift(g)
                giftToDelete = null
            },
            onDismiss = { giftToDelete = null }
        )
    }
}

@Composable
fun GiftItemCard(
    gift: GiftEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
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
                        text = gift.giverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${gift.relationship.ifEmpty { "Guest" }} • ${gift.giftItem}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalMaroon,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (gift.estimatedValue > 0) {
                    Text(
                        text = formatCurrency(gift.estimatedValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroon
                    )
                }
            }

            if (gift.returnGiftGiven.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Return Gift Given: ${gift.returnGiftGiven}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalGoldDark,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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

@Composable
fun GiftEditorDialog(
    weddingId: Long,
    existingGift: GiftEntity?,
    onDismiss: () -> Unit,
    onSave: (GiftEntity) -> Unit
) {
    var giverName by remember { mutableStateOf(existingGift?.giverName ?: "") }
    var relationship by remember { mutableStateOf(existingGift?.relationship ?: "") }
    var item by remember { mutableStateOf(existingGift?.giftItem ?: "Shagun Envelope") }
    var valueStr by remember { mutableStateOf(existingGift?.estimatedValue?.toInt()?.toString() ?: "2100") }
    var returnGift by remember { mutableStateOf(existingGift?.returnGiftGiven ?: "") }
    var notes by remember { mutableStateOf(existingGift?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingGift == null) "Add Shagun / Gift" else "Edit Shagun",
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = giverName,
                    onValueChange = { giverName = it },
                    label = { Text("Giver / Family Name *") },
                    placeholder = { Text("e.g. Ramesh Maheshwari & Family") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("giver_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = { Text("Relationship") },
                        placeholder = { Text("e.g. Mama Ji, Chacha Ji") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Shagun Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = item,
                    onValueChange = { item = it },
                    label = { Text("Gift / Shagun Item *") },
                    placeholder = { Text("e.g. Cash / Silver Coin / Mithai") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = returnGift,
                    onValueChange = { returnGift = it },
                    label = { Text("Return Gift Given") },
                    placeholder = { Text("e.g. Dry Fruit Hamper, Silver Bowl") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (giverName.isNotBlank() && item.isNotBlank()) {
                        val valAmt = valueStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            GiftEntity(
                                id = existingGift?.id ?: 0,
                                weddingId = weddingId,
                                giverName = giverName.trim(),
                                relationship = relationship.trim(),
                                giftItem = item.trim(),
                                estimatedValue = valAmt,
                                returnGiftGiven = returnGift.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_gift_btn")
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
