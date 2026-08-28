package com.example.ui.screens.vendors

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.local.entities.VendorEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoyalStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun VendorListScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val vendors by viewModel.vendors.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var vendorToEdit by remember { mutableStateOf<VendorEntity?>(null) }
    var vendorToDelete by remember { mutableStateOf<VendorEntity?>(null) }

    val totalContract = vendors.sumOf { it.totalContractValue }
    val totalAdvance = vendors.sumOf { it.advancePaid }
    val totalPending = maxOf(0.0, totalContract - totalAdvance)

    val filteredVendors = vendors.filter { v ->
        v.name.contains(searchQuery, ignoreCase = true) ||
                v.serviceType.contains(searchQuery, ignoreCase = true) ||
                v.contactNumber.contains(searchQuery, ignoreCase = true)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoyalStatCard(
                        title = "Total Contracts",
                        value = formatCurrency(totalContract),
                        subtitle = "${vendors.size} Vendors",
                        icon = Icons.Default.Store,
                        iconTint = RoyalMaroon,
                        modifier = Modifier.weight(1f)
                    )
                    RoyalStatCard(
                        title = "Pending Dues",
                        value = formatCurrency(totalPending),
                        subtitle = "Advance: ${formatCurrency(totalAdvance)}",
                        icon = Icons.Default.Store,
                        iconTint = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search vendor name, service or contact...") },
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

            if (filteredVendors.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Vendors Added",
                        message = "Tap '+ Add Vendor' to track Caterers, Decorators, Photographers, Band & Dhol, and Pandit Ji.",
                        icon = Icons.Default.Store
                    )
                }
            } else {
                items(filteredVendors, key = { it.id }) { vendor ->
                    VendorItemCard(
                        vendor = vendor,
                        onEdit = { vendorToEdit = vendor },
                        onDelete = { vendorToDelete = vendor }
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
                .testTag("add_vendor_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Vendor")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Vendor", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        VendorEditorDialog(
            weddingId = currentWedding!!.id,
            existingVendor = null,
            onDismiss = { showAddDialog = false },
            onSave = { newVendor ->
                viewModel.addVendor(newVendor)
                showAddDialog = false
            }
        )
    }

    vendorToEdit?.let { v ->
        VendorEditorDialog(
            weddingId = v.weddingId,
            existingVendor = v,
            onDismiss = { vendorToEdit = null },
            onSave = { updated ->
                viewModel.updateVendor(updated)
                vendorToEdit = null
            }
        )
    }

    vendorToDelete?.let { v ->
        ConfirmDialog(
            title = "Delete Vendor?",
            message = "Are you sure you want to remove '${v.name}' from your vendor list?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteVendor(v)
                vendorToDelete = null
            },
            onDismiss = { vendorToDelete = null }
        )
    }
}

@Composable
fun VendorItemCard(
    vendor: VendorEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingAmount = maxOf(0.0, vendor.totalContractValue - vendor.advancePaid)
    val status = if (vendor.advancePaid >= vendor.totalContractValue && vendor.totalContractValue > 0) "PAID" else if (vendor.advancePaid > 0) "BOOKED" else "PENDING"

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
                        text = vendor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = vendor.serviceType,
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalGoldDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(status = status)
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
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Total Contract", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = formatCurrency(vendor.totalContractValue), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Advance Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = formatCurrency(vendor.advancePaid), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Pending Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = formatCurrency(pendingAmount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (pendingAmount > 0) RoyalMaroon else SuccessGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (vendor.contactNumber.isNotEmpty()) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = vendor.contactNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row {
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
fun VendorEditorDialog(
    weddingId: Long,
    existingVendor: VendorEntity?,
    onDismiss: () -> Unit,
    onSave: (VendorEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingVendor?.name ?: "") }
    var serviceType by remember { mutableStateOf(existingVendor?.serviceType ?: "Caterer") }
    var contactNumber by remember { mutableStateOf(existingVendor?.contactNumber ?: "") }
    var address by remember { mutableStateOf(existingVendor?.address ?: "") }
    var totalContractStr by remember { mutableStateOf(existingVendor?.totalContractValue?.toInt()?.toString() ?: "100000") }
    var advancePaidStr by remember { mutableStateOf(existingVendor?.advancePaid?.toInt()?.toString() ?: "20000") }
    var notes by remember { mutableStateOf(existingVendor?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingVendor == null) "Add Wedding Vendor" else "Edit Vendor",
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
                    label = { Text("Vendor / Business Name *") },
                    placeholder = { Text("e.g. Royal Marwar Caterers") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vendor_name_input")
                )
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Type") },
                    placeholder = { Text("e.g. Caterer, Decorator, Band & Dhol, Pandit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact Number") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("City / Address") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalContractStr,
                        onValueChange = { totalContractStr = it },
                        label = { Text("Contract (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = advancePaidStr,
                        onValueChange = { advancePaidStr = it },
                        label = { Text("Advance (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Contract Inclusions & Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val contract = totalContractStr.toDoubleOrNull() ?: 0.0
                        val advance = advancePaidStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            VendorEntity(
                                id = existingVendor?.id ?: 0,
                                weddingId = weddingId,
                                name = name.trim(),
                                serviceType = serviceType.trim(),
                                contactNumber = contactNumber.trim(),
                                address = address.trim(),
                                totalContractValue = contract,
                                advancePaid = advance,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_vendor_btn")
            ) {
                Text("Save Vendor", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
