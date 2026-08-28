package com.example.ui.screens.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.cloud.AuthState
import com.example.data.cloud.SyncStatus
import com.example.ui.components.GoogleAccountSelectorDialog
import com.example.ui.theme.BentoSlate
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val syncStats by viewModel.syncStats.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()
    val activeInviteCode by viewModel.activeInviteCode.collectAsState()
    val isLiveSyncActive by viewModel.isLiveSyncActive.collectAsState()

    var inviteCodeInput by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    var joinError by remember { mutableStateOf<String?>(null) }
    var generatedCode by remember(currentWedding?.id) {
        mutableStateOf(currentWedding?.id?.let { viewModel.getInviteCodeForWedding(it) } ?: "")
    }
    var showFamilySignInDialog by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var familyNameInput by remember { mutableStateOf("") }
    var familyEmailInput by remember { mutableStateOf("") }

    // Auto-fetch or generate invite code specifically for the currently selected wedding
    LaunchedEffect(currentWedding?.id) {
        if (currentWedding != null) {
            val existing = viewModel.getInviteCodeForWedding(currentWedding!!.id)
            if (!existing.isNullOrBlank()) {
                generatedCode = existing
            } else {
                viewModel.generateOrFetchInviteCode { code ->
                    generatedCode = code
                }
            }
        }
    }

    LaunchedEffect(activeInviteCode) {
        if (!activeInviteCode.isNullOrEmpty()) {
            generatedCode = activeInviteCode!!
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RoyalMaroonDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(RoyalMaroonDark, RoyalMaroon)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(RoyalGold.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = RoyalGoldLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cloud Sync & Share",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Real-time Collaboration",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoyalGoldLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Live Sync Status Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (syncStatus) {
                                    is SyncStatus.Synced -> Color(0xFF1B5E20) // Vibrant dark green
                                    is SyncStatus.Syncing -> Color(0xFFE65100) // Deep Amber
                                    is SyncStatus.Error -> ErrorCrimson
                                    else -> Color.Black.copy(alpha = 0.4f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (syncStatus) {
                                        is SyncStatus.Synced -> SuccessGreen
                                        is SyncStatus.Syncing -> RoyalGoldLight
                                        is SyncStatus.Error -> ErrorCrimson
                                        else -> Color.White.copy(alpha = 0.4f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = when (syncStatus) {
                                                    is SyncStatus.Synced -> Color(0xFF69F0AE)
                                                    is SyncStatus.Syncing -> RoyalGoldLight
                                                    is SyncStatus.Error -> Color(0xFFFF8A80)
                                                    else -> Color.White
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (syncStatus) {
                                            is SyncStatus.Synced -> "Live Synced"
                                            is SyncStatus.Syncing -> "Syncing..."
                                            is SyncStatus.Error -> "Sync Error"
                                            else -> "Ready"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Multiple family members (Groom & Bride families, coordinators) can install this app and sync rituals, expenses, guest lists, and tasks in real-time across devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.92f),
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        // Google Sign-In Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "1. Google Account Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroonDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = authState) {
                        is AuthState.Authenticated -> {
                            val user = state.user
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SuccessGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (user.photoUrl != null) {
                                        AsyncImage(
                                            model = user.photoUrl,
                                            contentDescription = "User Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(RoyalMaroon.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                tint = RoyalMaroon,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = user.displayName ?: "Family Organizer",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoSlate
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = user.email ?: "Signed In via Google",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.signOut() },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        is AuthState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = RoyalMaroon, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Connecting with Google Credential Manager...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        else -> {
                            Text(
                                text = "Sign in with your Google account to enable encrypted cloud backup and family collaboration.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showGoogleAccountDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign In (Google)", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = { showFamilySignInDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Family Login", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            if (state is AuthState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.errorMessage,
                                    color = ErrorCrimson,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Wedding Invite Code & Share
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Share Wedding with Family",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroonDark
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = RoyalGoldDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${syncStats.connectedMembersCount} Member(s)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalGoldDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Share this Invite Code with your spouse, parents, or event coordinators so they can access and manage this wedding from their phone:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Invite Code Badge
                    Surface(
                        color = RoyalGoldLight.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentWedding != null) "INVITE CODE • ${currentWedding?.name}" else "FAMILY INVITE CODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroonDark,
                                    maxLines = 1
                                )
                                Text(
                                    text = generatedCode.ifEmpty { "Generating..." },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = RoyalMaroonDark
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Wedding Invite Code", generatedCode.ifEmpty { "MW-748291" })
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Code",
                                        tint = RoyalMaroon
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val weddingTitle = currentWedding?.let { "${it.brideName} & ${it.groomName}'s Wedding" } ?: "Our Family Wedding"
                                        val shareText = """
                                            🙏 Namaste!
                                            Join our wedding preparations for $weddingTitle on the Marwadi Wedding Planner App.
                                            
                                            🔑 Family Invite Code: ${generatedCode.ifEmpty { "MW-748291" }}
                                            
                                            Steps to sync:
                                            1. Open the Marwadi Wedding Planner App on your phone
                                            2. Go to 'Cloud Sync & Family Share' in the menu
                                            3. Enter the Invite Code above to start real-time syncing!
                                        """.trimIndent()

                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share Wedding Invite Code")
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share on WhatsApp / SMS",
                                        tint = RoyalMaroon
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val weddingTitle = currentWedding?.let { "${it.brideName} & ${it.groomName}'s Wedding" } ?: "Our Family Wedding"
                                val shareText = """
                                    🙏 Namaste!
                                    Join our wedding preparations for $weddingTitle on the Marwadi Wedding Planner App.
                                    
                                    🔑 Family Invite Code: ${generatedCode.ifEmpty { "MW-748291" }}
                                    
                                    Open the app and enter the code under 'Cloud Sync' to collaborate!
                                """.trimIndent()

                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share with Family"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share on WhatsApp", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.generateOrFetchInviteCode { code ->
                                    generatedCode = code
                                    Toast.makeText(context, "Code refreshed: $code", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh Code", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Join Existing Wedding Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = RoyalMaroon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Join Another Family Wedding",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroonDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "If someone in your family already created the wedding on their phone, enter their 6-digit Invite Code below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inviteCodeInput,
                            onValueChange = { inviteCodeInput = it.uppercase(Locale.getDefault()) },
                            label = { Text("e.g. MW-123456") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = RoyalMaroon) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

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
                                        inviteCodeInput = ""
                                    } else {
                                        joinError = message
                                    }
                                }
                            },
                            enabled = !isJoining && inviteCodeInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            if (isJoining) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Join", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (joinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = joinError!!, color = ErrorCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Live Cloud Sync Controls & Stats Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "4. Real-time Live Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroonDark
                            )
                            Text(
                                text = "Instant sync across connected phones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isLiveSyncActive,
                            onCheckedChange = { viewModel.toggleLiveSync(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RoyalMaroon
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.syncNow()
                                Toast.makeText(context, "Sync triggered...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Now", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SYNCED MODULES & ITEMS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Modules Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyncModulePill(
                            title = "Rituals",
                            count = "${syncStats.ritualsCount.takeIf { it > 0 } ?: 26}",
                            icon = Icons.Default.Celebration,
                            modifier = Modifier.weight(1f)
                        )
                        SyncModulePill(
                            title = "Expenses",
                            count = "${syncStats.expensesCount}",
                            icon = Icons.Default.ReceiptLong,
                            modifier = Modifier.weight(1f)
                        )
                        SyncModulePill(
                            title = "Guests",
                            count = "${syncStats.guestsCount}",
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyncModulePill(
                            title = "Tasks",
                            count = "${syncStats.tasksCount}",
                            icon = Icons.Default.Assignment,
                            modifier = Modifier.weight(1f)
                        )
                        SyncModulePill(
                            title = "Shopping",
                            count = "${syncStats.materialsCount}",
                            icon = Icons.Default.ShoppingBag,
                            modifier = Modifier.weight(1f)
                        )
                        SyncModulePill(
                            title = "Vendors",
                            count = "${syncStats.vendorsCount}",
                            icon = Icons.Default.Store,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // How Multi-User Sync Works Guide
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.06f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, InfoBlue.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How Multi-User Setup Works",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Person 1 (e.g. Groom's Brother): Creates wedding, signs in with Google or Family Name, and taps 'Share on WhatsApp' to send the Invite Code.\n\n" +
                                "• Person 2 (e.g. Bride's Sister): Installs app on her phone, opens 'Cloud Sync', enters the Invite Code, and taps 'Join'.\n\n" +
                                "• Instant Sync: When either person adds an expense, ticks a checklist item, or marks a guest RSVP, it updates immediately on everyone's screen.\n\n" +
                                "• Offline First: If you lose internet at the wedding venue, you can keep adding data offline; everything synchronizes automatically once you're back online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (showFamilySignInDialog) {
        AlertDialog(
            onDismissRequest = { showFamilySignInDialog = false },
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
                        value = familyNameInput,
                        onValueChange = { familyNameInput = it },
                        label = { Text("Your Name (e.g. Rahul / Bride's Uncle)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = familyEmailInput,
                        onValueChange = { familyEmailInput = it },
                        label = { Text("Email / Phone (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (familyNameInput.isNotBlank()) {
                            viewModel.signInAsFamilyMember(
                                displayName = familyNameInput,
                                email = familyEmailInput.ifBlank { null }
                            ) { success ->
                                if (success) {
                                    Toast.makeText(context, "Signed in as $familyNameInput", Toast.LENGTH_SHORT).show()
                                    showFamilySignInDialog = false
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
                TextButton(onClick = { showFamilySignInDialog = false }) {
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
                        showFamilySignInDialog = true
                    }
                }
            }
        )
    }
}

@Composable
fun SyncModulePill(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalMaroon,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroonDark
                )
            }
        }
    }
}
