package com.example.ui.screens.notes

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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
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
import com.example.data.local.entities.NoteEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.viewmodel.WeddingViewModel

val NOTE_CATEGORIES = listOf(
    "All Notes",
    "Traditional Geet & Songs",
    "Mantras & Vidhi Notes",
    "Family Custom Notes",
    "Dance & Sangeet Ideas"
)

@Composable
fun NotesIdeasScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    var selectedCategory by remember { mutableStateOf("All Notes") }
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val filteredNotes = notes.filter { n ->
        selectedCategory == "All Notes" || n.category == selectedCategory
    }

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
                            text = "Traditional Songs & Family Notes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroonDark
                        )
                        Text(
                            text = "Store Marwadi Vivah Geet (Banna Banni, Mayra Geet, Ghoomar), Vidhi mantras, and custom family traditions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(NOTE_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Notes in this Category",
                        message = "Tap '+ Add Note' to write down traditional songs, mantras, or reminders.",
                        icon = Icons.Default.Note
                    )
                }
            } else {
                items(filteredNotes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier
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
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = note.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalGoldDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { noteToEdit = note }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { noteToDelete = note }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
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
                .testTag("add_note_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Note", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog && currentWedding != null) {
        NoteEditorDialog(
            weddingId = currentWedding!!.id,
            existingNote = null,
            onDismiss = { showAddDialog = false },
            onSave = { newNote ->
                viewModel.addNote(newNote)
                showAddDialog = false
            }
        )
    }

    noteToEdit?.let { n ->
        NoteEditorDialog(
            weddingId = n.weddingId,
            existingNote = n,
            onDismiss = { noteToEdit = null },
            onSave = { updated ->
                viewModel.updateNote(updated)
                noteToEdit = null
            }
        )
    }

    noteToDelete?.let { n ->
        ConfirmDialog(
            title = "Delete Note?",
            message = "Are you sure you want to delete '${n.title}'?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteNote(n)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}

@Composable
fun NoteEditorDialog(
    weddingId: Long,
    existingNote: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var category by remember { mutableStateOf(existingNote?.category ?: "Traditional Geet & Songs") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingNote == null) "Add Traditional Note / Song" else "Edit Note",
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("e.g. Mayra Ghoomar Geet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Traditional Geet, Mantra, Custom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Song Lyrics / Notes / Instructions *") },
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("note_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSave(
                            NoteEntity(
                                id = existingNote?.id ?: 0,
                                weddingId = weddingId,
                                title = title.trim(),
                                category = category.trim(),
                                content = content.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("save_note_btn")
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
