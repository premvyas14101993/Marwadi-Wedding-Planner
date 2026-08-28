package com.example.ui.screens.family

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.PersonEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun FamilyManagementScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val people by viewModel.people.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }
    var personToDelete by remember { mutableStateOf<PersonEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Family Roles & Delegations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroonDark
                        )
                        Text(
                            text = "Assign critical responsibilities (Finance, Mayra Lead, Barat Coordinator, Sangeet Lead) to family members.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (people.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Family Roles Assigned",
                        message = "Tap '+ Add Family Member' to assign wedding coordination roles.",
                        icon = Icons.Default.People
                    )
                }
            } else {
                items(people, key = { it.id }) { person ->
                    PersonCard(
                        person = person,
                        onEdit = { personToEdit = person },
                        onDelete = { personToDelete = person }
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
                .testTag("add_family_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Member", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        FamilyEditorDialog(
            weddingId = currentWedding!!.id,
            existingPerson = null,
            onDismiss = { showAddDialog = false },
            onSave = { newPerson ->
                viewModel.addPerson(newPerson)
                showAddDialog = false
            }
        )
    }

    personToEdit?.let { p ->
        FamilyEditorDialog(
            weddingId = p.weddingId,
            existingPerson = p,
            onDismiss = { personToEdit = null },
            onSave = { updated ->
                viewModel.updatePerson(updated)
                personToEdit = null
            }
        )
    }

    personToDelete?.let { p ->
        ConfirmDialog(
            title = "Delete Family Member?",
            message = "Remove '${p.name}' from family roles?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePerson(p)
                personToDelete = null
            },
            onDismiss = { personToDelete = null }
        )
    }
}

@Composable
fun PersonCard(
    person: PersonEntity,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoyalMaroon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = person.relationship, style = MaterialTheme.typography.bodySmall, color = RoyalGoldDark, fontWeight = FontWeight.Medium)
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

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = "Assigned Responsibility:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = person.role, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = RoyalMaroon)
                }
            }

            if (person.phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = person.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun FamilyEditorDialog(
    weddingId: Long,
    existingPerson: PersonEntity?,
    onDismiss: () -> Unit,
    onSave: (PersonEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingPerson?.name ?: "") }
    var relationship by remember { mutableStateOf(existingPerson?.relationship ?: "") }
    var role by remember { mutableStateOf(existingPerson?.role ?: "") }
    var phone by remember { mutableStateOf(existingPerson?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingPerson == null) "Add Family Lead" else "Edit Role",
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    placeholder = { Text("e.g. Ramesh Maheshwari") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("person_name_input")
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship / Relation") },
                    placeholder = { Text("e.g. Father, Mama Ji, Cousin, Bhabhi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Assigned Responsibility *") },
                    placeholder = { Text("e.g. Overall Finance & Cash Handler, Mayra Leader") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            PersonEntity(
                                id = existingPerson?.id ?: 0,
                                weddingId = weddingId,
                                name = name.trim(),
                                relationship = relationship.trim(),
                                role = role.trim().ifEmpty { "General Wedding Coordinator" },
                                phone = phone.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_person_btn")
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
