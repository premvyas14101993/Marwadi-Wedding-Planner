package com.example.ui.screens.guests

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.entities.GuestEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoyalStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun GuestManagementScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val guests by viewModel.guests.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var selectedSideFilter by remember { mutableStateOf("ALL") } // ALL, GROOM_SIDE, BRIDE_SIDE, COMMON
    var selectedRsvpFilter by remember { mutableStateOf("ALL") } // ALL, ACCEPTED, PENDING, DECLINED, TENTATIVE
    var selectedHotelFilter by remember { mutableStateOf("ALL") } // ALL, REQUIRED, ASSIGNED, NOT_REQUIRED
    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var guestToEdit by remember { mutableStateOf<GuestEntity?>(null) }
    var guestToDelete by remember { mutableStateOf<GuestEntity?>(null) }

    val totalFamilies = guests.size
    val totalHeadcount = guests.sumOf { it.numberOfMembers }
    val confirmedHeadcount = guests.filter { it.rsvpStatus == "ACCEPTED" }.sumOf { it.numberOfMembers }
    val pendingCount = guests.count { it.rsvpStatus == "PENDING" }
    val hotelRequiredCount = guests.count { it.accommodationRequired }

    val filteredGuests = guests.filter { g ->
        val matchesSide = when (selectedSideFilter) {
            "GROOM_SIDE" -> g.side == "GROOM_SIDE"
            "BRIDE_SIDE" -> g.side == "BRIDE_SIDE"
            "COMMON" -> g.side == "COMMON"
            else -> true
        }
        val matchesRsvp = when (selectedRsvpFilter) {
            "ACCEPTED" -> g.rsvpStatus == "ACCEPTED"
            "PENDING" -> g.rsvpStatus == "PENDING"
            "DECLINED" -> g.rsvpStatus == "DECLINED"
            "TENTATIVE" -> g.rsvpStatus == "TENTATIVE"
            else -> true
        }
        val matchesHotel = when (selectedHotelFilter) {
            "REQUIRED" -> g.accommodationRequired
            "ASSIGNED" -> g.accommodationRequired && g.hotelRoomAllocated.isNotBlank()
            "NOT_REQUIRED" -> !g.accommodationRequired
            else -> true
        }
        val matchesSearch = g.name.contains(searchQuery, ignoreCase = true) ||
                g.city.contains(searchQuery, ignoreCase = true) ||
                g.familyName.contains(searchQuery, ignoreCase = true) ||
                g.phone.contains(searchQuery, ignoreCase = true) ||
                g.hotelRoomAllocated.contains(searchQuery, ignoreCase = true)

        matchesSide && matchesRsvp && matchesHotel && matchesSearch
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
                        title = "Total Headcount",
                        value = "$totalHeadcount Guests",
                        subtitle = "$totalFamilies Families",
                        icon = Icons.Default.Groups,
                        iconTint = RoyalMaroon,
                        modifier = Modifier.weight(1f)
                    )
                    RoyalStatCard(
                        title = "Confirmed RSVP",
                        value = "$confirmedHeadcount Guests",
                        subtitle = "$pendingCount Pending",
                        icon = Icons.Default.CheckCircle,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search guest name, city, family or phone...") },
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

            // Side Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedSideFilter == "ALL",
                            onClick = { selectedSideFilter = "ALL" },
                            label = { Text("All Sides") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedSideFilter == "GROOM_SIDE",
                            onClick = { selectedSideFilter = "GROOM_SIDE" },
                            label = { Text("Groom's Side") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedSideFilter == "BRIDE_SIDE",
                            onClick = { selectedSideFilter = "BRIDE_SIDE" },
                            label = { Text("Bride's Side") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedSideFilter == "COMMON",
                            onClick = { selectedSideFilter = "COMMON" },
                            label = { Text("Mutual / Both") }
                        )
                    }
                }
            }

            // RSVP Status Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedRsvpFilter == "ALL",
                            onClick = { selectedRsvpFilter = "ALL" },
                            label = { Text("All RSVP") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRsvpFilter == "ACCEPTED",
                            onClick = { selectedRsvpFilter = "ACCEPTED" },
                            label = { Text("Accepted") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRsvpFilter == "PENDING",
                            onClick = { selectedRsvpFilter = "PENDING" },
                            label = { Text("Pending") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedRsvpFilter == "DECLINED",
                            onClick = { selectedRsvpFilter = "DECLINED" },
                            label = { Text("Declined") }
                        )
                    }
                }
            }

            // Accommodation Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedHotelFilter == "ALL",
                            onClick = { selectedHotelFilter = "ALL" },
                            label = { Text("All Stay") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedHotelFilter == "REQUIRED",
                            onClick = { selectedHotelFilter = "REQUIRED" },
                            label = { Text("🏨 Needs Room ($hotelRequiredCount)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedHotelFilter == "ASSIGNED",
                            onClick = { selectedHotelFilter = "ASSIGNED" },
                            label = { Text("🔑 Room Assigned") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedHotelFilter == "NOT_REQUIRED",
                            onClick = { selectedHotelFilter = "NOT_REQUIRED" },
                            label = { Text("No Stay Needed") }
                        )
                    }
                }
            }

            // Guest Cards
            if (filteredGuests.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Guests Found",
                        message = "Tap '+ Add Guest' to add wedding invitees, RSVP status, and hotel accommodation requirements.",
                        icon = Icons.Default.Groups
                    )
                }
            } else {
                items(filteredGuests, key = { it.id }) { guest ->
                    GuestItemCard(
                        guest = guest,
                        onEdit = { guestToEdit = guest },
                        onDelete = { guestToDelete = guest },
                        onToggleRsvp = {
                            val next = when (guest.rsvpStatus) {
                                "PENDING" -> "ACCEPTED"
                                "ACCEPTED" -> "DECLINED"
                                "DECLINED" -> "TENTATIVE"
                                else -> "PENDING"
                            }
                            viewModel.updateGuest(guest.copy(rsvpStatus = next))
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
                .testTag("add_guest_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Guest")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Guest", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        GuestEditorDialog(
            weddingId = currentWedding!!.id,
            existingGuest = null,
            onDismiss = { showAddDialog = false },
            onSave = { newGuest ->
                viewModel.addGuest(newGuest)
                showAddDialog = false
            }
        )
    }

    guestToEdit?.let { g ->
        GuestEditorDialog(
            weddingId = g.weddingId,
            existingGuest = g,
            onDismiss = { guestToEdit = null },
            onSave = { updated ->
                viewModel.updateGuest(updated)
                guestToEdit = null
            }
        )
    }

    guestToDelete?.let { g ->
        ConfirmDialog(
            title = "Delete Guest?",
            message = "Are you sure you want to remove '${g.name}' from the wedding guest list?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteGuest(g)
                guestToDelete = null
            },
            onDismiss = { guestToDelete = null }
        )
    }
}

@Composable
fun GuestItemCard(
    guest: GuestEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleRsvp: () -> Unit,
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
                        text = guest.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (guest.familyName.isNotEmpty()) {
                        Text(
                            text = guest.familyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier.clickable { onToggleRsvp() }
                ) {
                    StatusBadge(status = guest.rsvpStatus)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    Text(
                        text = "Members: ${guest.numberOfMembers}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroon
                    )

                    Text(
                        text = "Side: ${guest.side.replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )

                    if (guest.accommodationRequired) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = InfoBlue.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Hotel, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (guest.hotelRoomAllocated.isNotBlank()) "Room: ${guest.hotelRoomAllocated}" else "Stay: Yes (Room Pending)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InfoBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Stay: No",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    if (guest.city.isNotEmpty()) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = guest.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (guest.phone.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = guest.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).testTag("edit_guest_${guest.id}")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).testTag("delete_guest_${guest.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestEditorDialog(
    weddingId: Long,
    existingGuest: GuestEntity?,
    onDismiss: () -> Unit,
    onSave: (GuestEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingGuest?.name ?: "") }
    var familyName by remember { mutableStateOf(existingGuest?.familyName ?: "") }
    var membersStr by remember { mutableStateOf(existingGuest?.numberOfMembers?.toString() ?: "2") }
    var side by remember { mutableStateOf(existingGuest?.side ?: "BRIDE_SIDE") }
    var rsvpStatus by remember { mutableStateOf(existingGuest?.rsvpStatus ?: "PENDING") }
    var phone by remember { mutableStateOf(existingGuest?.phone ?: "") }
    var city by remember { mutableStateOf(existingGuest?.city ?: "Jodhpur") }
    var needsHotel by remember { mutableStateOf(existingGuest?.accommodationRequired ?: false) }
    var hotelName by remember { mutableStateOf(existingGuest?.hotelRoomAllocated ?: "") }

    var sideExpanded by remember { mutableStateOf(false) }
    var rsvpExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingGuest == null) "Add Guest / Family" else "Edit Guest",
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
                    label = { Text("Primary Guest / Head Name *") },
                    placeholder = { Text("e.g. Ramesh Chand Maheshwari") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("guest_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = membersStr,
                        onValueChange = { membersStr = it },
                        label = { Text("No. of Members") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("Family Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Side Selector
                ExposedDropdownMenuBox(
                    expanded = sideExpanded,
                    onExpandedChange = { sideExpanded = !sideExpanded }
                ) {
                    OutlinedTextField(
                        value = side.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Side") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sideExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sideExpanded,
                        onDismissRequest = { sideExpanded = false }
                    ) {
                        listOf("BRIDE_SIDE", "GROOM_SIDE", "COMMON").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.replace("_", " ")) },
                                onClick = {
                                    side = s
                                    sideExpanded = false
                                }
                            )
                        }
                    }
                }

                // RSVP Status
                ExposedDropdownMenuBox(
                    expanded = rsvpExpanded,
                    onExpandedChange = { rsvpExpanded = !rsvpExpanded }
                ) {
                    OutlinedTextField(
                        value = rsvpStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("RSVP Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rsvpExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = rsvpExpanded,
                        onDismissRequest = { rsvpExpanded = false }
                    ) {
                        listOf("PENDING", "ACCEPTED", "DECLINED", "TENTATIVE").forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    rsvpStatus = r
                                    rsvpExpanded = false
                                }
                            )
                        }
                    }
                }

                // Accommodation (Yes/No option with Room Assignment Column)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hotel,
                                    contentDescription = null,
                                    tint = RoyalMaroon,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Accommodation",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (needsHotel) "Yes (Provide room allocation below)" else "No accommodation required",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (needsHotel) InfoBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = needsHotel,
                                onCheckedChange = { checked ->
                                    needsHotel = checked
                                    if (!checked) {
                                        hotelName = ""
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = RoyalMaroon
                                ),
                                modifier = Modifier.testTag("accommodation_switch")
                            )
                        }

                        if (needsHotel) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = hotelName,
                                onValueChange = { hotelName = it },
                                label = { Text("Assigned Room No. / Hotel *") },
                                placeholder = { Text("e.g. Room 204, Marwar Heritage") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        tint = RoyalMaroon
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_room_input")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val members = membersStr.toIntOrNull() ?: 1
                        onSave(
                            GuestEntity(
                                id = existingGuest?.id ?: 0,
                                weddingId = weddingId,
                                name = name.trim(),
                                familyName = familyName.trim(),
                                numberOfMembers = members,
                                side = side,
                                rsvpStatus = rsvpStatus,
                                phone = phone.trim(),
                                city = city.trim(),
                                accommodationRequired = needsHotel,
                                hotelRoomAllocated = hotelName.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_guest_btn")
            ) {
                Text("Save Guest", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
