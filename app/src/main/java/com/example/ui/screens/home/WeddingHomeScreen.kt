package com.example.ui.screens.home

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.cloud.AuthState
import com.example.data.cloud.SyncStatus
import com.example.data.local.entities.WeddingEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GoogleAccountSelectorDialog
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.BentoSlate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.RoyalMaroonLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeddingHomeScreen(
    viewModel: WeddingViewModel,
    onWeddingSelected: (Long) -> Unit,
    onNavigateToSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weddings by viewModel.allWeddings.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showFamilyLoginDialog by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var familyLoginName by remember { mutableStateOf("") }
    var familyLoginEmail by remember { mutableStateOf("") }
    var inviteCodeInput by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    var joinError by remember { mutableStateOf<String?>(null) }
    var weddingToDelete by remember { mutableStateOf<WeddingEntity?>(null) }
    var weddingToDuplicate by remember { mutableStateOf<WeddingEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Marwadi Wedding Planner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Royal Marwadi Vivah Prabandhan",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoyalGoldLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalMaroonDark
                ),
                actions = {
                    // Cloud Sync / Google Account Action Button
                    IconButton(
                        onClick = onNavigateToSync,
                        modifier = Modifier.testTag("home_cloud_sync_btn")
                    ) {
                        when (val state = authState) {
                            is AuthState.Authenticated -> {
                                if (state.user.photoUrl != null) {
                                    AsyncImage(
                                        model = state.user.photoUrl,
                                        contentDescription = "User Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, RoyalGold, CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google Account",
                                        tint = RoyalGoldLight
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sign In & Cloud Sync",
                                    tint = RoyalGoldLight
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.resetToDemoData {
                                val currentId = viewModel.selectedWeddingId.value
                                if (currentId != null) {
                                    onWeddingSelected(currentId)
                                }
                            }
                        },
                        modifier = Modifier.testTag("seed_demo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = "Load Demo Wedding",
                            tint = RoyalGoldLight
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = RoyalMaroon,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_wedding_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Wedding")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "New Wedding", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Sign In & Cloud Collaboration Banner
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CardBorderGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            RoyalMaroonDark.copy(alpha = 0.04f),
                                            RoyalGoldLight.copy(alpha = 0.12f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
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
                                            .size(38.dp)
                                            .background(RoyalMaroon.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = null,
                                            tint = RoyalMaroon,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Google Account & Family Sync",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalMaroonDark
                                        )
                                        Text(
                                            text = when (val auth = authState) {
                                                is AuthState.Authenticated -> "Signed in as ${auth.user.displayName ?: auth.user.email ?: "User"}"
                                                else -> "Sign in to backup & collaborate with family"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            when (val state = authState) {
                                is AuthState.Authenticated -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = onNavigateToSync,
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Cloud Sync Hub", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                        }

                                        OutlinedButton(
                                            onClick = { showJoinDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Join Wedding", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                                is AuthState.Loading -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = RoyalMaroon)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Signing in with Google...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                else -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                showGoogleAccountDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Sign In", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { showFamilyLoginDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Profile", style = MaterialTheme.typography.labelMedium)
                                        }

                                        OutlinedButton(
                                            onClick = { showJoinDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Join", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (weddings.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Wedding Projects Yet",
                            message = "Create your first royal Marwadi wedding project, join with a family invite code, or load the demo wedding.",
                            icon = Icons.Default.Celebration,
                            buttonText = "+ Create New Wedding",
                            onButtonClick = { showCreateDialog = true }
                        )
                    }
                } else {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(RoyalMaroon),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = RoyalGoldLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Your Wedding Projects (${weddings.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Select a wedding to manage rituals, expenses, budget, and guests.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    items(weddings, key = { it.id }) { wedding ->
                        val projectInviteCode = viewModel.getInviteCodeForWedding(wedding.id)
                        WeddingProjectCard(
                            wedding = wedding,
                            inviteCode = projectInviteCode,
                            isSelected = wedding.id == currentWedding?.id,
                            onOpen = {
                                viewModel.selectWedding(wedding.id)
                                onWeddingSelected(wedding.id)
                            },
                            onDuplicate = { weddingToDuplicate = wedding },
                            onDelete = { weddingToDelete = wedding }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Join with Family Code Dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = {
                showJoinDialog = false
                joinError = null
                inviteCodeInput = ""
            },
            icon = {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Join Family Wedding", fontWeight = FontWeight.Bold, color = RoyalMaroonDark)
            },
            text = {
                Column {
                    Text(
                        text = "Enter the 6-digit Invite Code shared by your family member to sync all rituals, expenses, and guest lists to your phone:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it.uppercase(Locale.getDefault()) },
                        label = { Text("Invite Code (e.g. MW-123456)") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = RoyalMaroon) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (joinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = joinError!!, color = ErrorCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteCodeInput.isBlank()) {
                            joinError = "Please enter an invite code"
                            return@Button
                        }
                        isJoining = true
                        joinError = null
                        viewModel.joinWeddingWithInviteCode(inviteCodeInput.trim()) { success, message ->
                            isJoining = false
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                showJoinDialog = false
                                val joinedId = viewModel.selectedWeddingId.value
                                inviteCodeInput = ""
                                if (joinedId != null) {
                                    onWeddingSelected(joinedId)
                                }
                            } else {
                                joinError = message
                            }
                        }
                    },
                    enabled = !isJoining && inviteCodeInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    if (isJoining) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Join Wedding", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGoogleAccountDialog) {
        GoogleAccountSelectorDialog(
            onDismiss = { showGoogleAccountDialog = false },
            onSelectAccount = { email, name ->
                viewModel.signInWithGoogleAccount(email, name) {
                    Toast.makeText(context, "Welcome $name!", Toast.LENGTH_SHORT).show()
                }
                showGoogleAccountDialog = false
            },
            onContinueWithCredentialManager = {
                showGoogleAccountDialog = false
                viewModel.signInWithGoogle { success, errorMsg ->
                    if (success) {
                        Toast.makeText(context, "Signed in via Google!", Toast.LENGTH_SHORT).show()
                    } else if (errorMsg != null && !errorMsg.contains("cancelled", ignoreCase = true)) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        showFamilyLoginDialog = true
                    }
                }
            }
        )
    }

    if (showFamilyLoginDialog) {
        AlertDialog(
            onDismissRequest = { showFamilyLoginDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = RoyalMaroon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Family Member Login", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your name or family role to enable cloud collaboration on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = familyLoginName,
                        onValueChange = { familyLoginName = it },
                        label = { Text("Your Name (e.g. Rahul / Bride's Uncle)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = familyLoginEmail,
                        onValueChange = { familyLoginEmail = it },
                        label = { Text("Email / Phone (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (familyLoginName.isNotBlank()) {
                            viewModel.signInAsFamilyMember(
                                displayName = familyLoginName,
                                email = familyLoginEmail.ifBlank { null }
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Signed in as $familyLoginName", Toast.LENGTH_SHORT).show()
                                    showFamilyLoginDialog = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFamilyLoginDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateWeddingDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, bride, groom, date, engDate, venue, city, family, budget, notes ->
                viewModel.createWedding(
                    name = name,
                    brideName = bride,
                    groomName = groom,
                    weddingDate = date,
                    engagementDate = engDate,
                    venue = venue,
                    city = city,
                    familyName = family,
                    overallBudget = budget,
                    notes = notes,
                    onCreated = { newId ->
                        onWeddingSelected(newId)
                    }
                )
                showCreateDialog = false
            }
        )
    }

    weddingToDelete?.let { wedding ->
        ConfirmDialog(
            title = "Delete Wedding Project?",
            message = "Are you sure you want to delete '${wedding.name}'? All rituals, expenses, guest lists, and tasks for this wedding will be permanently removed.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteWedding(wedding)
                weddingToDelete = null
            },
            onDismiss = { weddingToDelete = null }
        )
    }

    weddingToDuplicate?.let { wedding ->
        var newName by remember { mutableStateOf("${wedding.name} (Copy)") }
        AlertDialog(
            onDismissRequest = { weddingToDuplicate = null },
            title = { Text(text = "Duplicate Wedding Project", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Enter a name for the duplicated wedding template:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New Wedding Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.duplicateWedding(wedding.id, newName)
                        weddingToDuplicate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Duplicate", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { weddingToDuplicate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WeddingProjectCard(
    wedding: WeddingEntity,
    isSelected: Boolean,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    inviteCode: String? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()
    val diffMillis = wedding.weddingDate - now
    val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RoyalMaroon else CardBorderGold,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onOpen() }
            .testTag("wedding_card_${wedding.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wedding.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroonDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${wedding.groomName}  ❤️  ${wedding.brideName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (wedding.familyName.isNotEmpty()) {
                        Text(
                            text = wedding.familyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!inviteCode.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoyalGoldLight.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Code: $inviteCode",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroonDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Project") },
                            onClick = {
                                showMenu = false
                                onOpen()
                            },
                            leadingIcon = { Icon(Icons.Default.Celebration, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate Project") },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Project", color = ErrorCrimson) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorCrimson) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Venue & Date Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = RoyalGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDate(wedding.weddingDate),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = RoyalMaroon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (wedding.city.isNotEmpty()) "${wedding.venue}, ${wedding.city}" else wedding.venue.ifEmpty { "Venue TBD" },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial & Countdown highlights
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(wedding.overallBudget),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    }

                    Surface(
                        color = RoyalMaroon,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (daysRemaining > 0) "$daysRemaining Days Left" else "Wedding Today!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Open Action
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalMaroon
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Open Wedding Dashboard",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CreateWeddingDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        bride: String,
        groom: String,
        date: Long,
        engDate: Long?,
        venue: String,
        city: String,
        family: String,
        budget: Double,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var weddingName by remember { mutableStateOf("") }
    var brideName by remember { mutableStateOf("") }
    var groomName by remember { mutableStateOf("") }
    var weddingDateMillis by remember { mutableStateOf(System.currentTimeMillis() + (90L * 86400000L)) }
    var engagementDateMillis by remember { mutableStateOf<Long?>(null) }
    var venue by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Jodhpur, Rajasthan") }
    var familyName by remember { mutableStateOf("") }
    var budgetStr by remember { mutableStateOf("2000000") }
    var notes by remember { mutableStateOf("Traditional Marwadi royal wedding.") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cal = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val c = Calendar.getInstance()
                c.set(year, month, dayOfMonth)
                weddingDateMillis = c.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Wedding Project",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RoyalMaroonDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Automatically sets up 26 traditional Marwadi rituals, checklists, and inventory templates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = weddingName,
                    onValueChange = {
                        weddingName = it
                        errorMessage = null
                    },
                    label = { Text("Wedding Project Name *") },
                    placeholder = { Text("e.g. Rahul & Priya Wedding") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("wedding_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = groomName,
                        onValueChange = {
                            groomName = it
                            if (weddingName.isEmpty() && brideName.isNotEmpty()) {
                                weddingName = "$groomName & $brideName Wedding"
                            }
                        },
                        label = { Text("Groom Name *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("groom_name_input")
                    )
                    OutlinedTextField(
                        value = brideName,
                        onValueChange = {
                            brideName = it
                            if (weddingName.isEmpty() && groomName.isNotEmpty()) {
                                weddingName = "$groomName & $brideName Wedding"
                            }
                        },
                        label = { Text("Bride Name *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bride_name_input")
                    )
                }

                // Date Picker field
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = RoyalMaroon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Wedding Date: ${formatDate(weddingDateMillis)}")
                }

                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Family Name / Gotra") },
                    placeholder = { Text("e.g. Maheshwari & Vyas Pariwar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue / Palace") },
                        placeholder = { Text("e.g. Indana Palace") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        placeholder = { Text("e.g. Jodhpur") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Overall Budget (₹) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("budget_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Family Priorities") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ErrorCrimson,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (weddingName.isBlank()) {
                        errorMessage = "Please enter wedding project name."
                        return@Button
                    }
                    if (groomName.isBlank() || brideName.isBlank()) {
                        errorMessage = "Please enter both Bride and Groom names."
                        return@Button
                    }
                    val budget = budgetStr.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        weddingName,
                        brideName,
                        groomName,
                        weddingDateMillis,
                        engagementDateMillis,
                        venue,
                        city,
                        familyName,
                        budget,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                modifier = Modifier.testTag("submit_wedding_btn")
            ) {
                Text("Create Project", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
