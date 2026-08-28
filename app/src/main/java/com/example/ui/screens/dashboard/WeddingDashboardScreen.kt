package com.example.ui.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CountdownBanner
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.RoyalCard
import com.example.ui.components.RoyalSectionHeader
import com.example.ui.components.SimpleDonutChart
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoGold
import com.example.ui.theme.BentoSlate
import com.example.ui.theme.BentoSlateMuted
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ErrorCrimsonLight
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun WeddingDashboardScreen(
    viewModel: WeddingViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val wedding by viewModel.currentWedding.collectAsState()
    val rituals by viewModel.rituals.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val expenseByCategory by viewModel.expenseByCategory.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val guests by viewModel.guests.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val consolidatedShopping by viewModel.consolidatedShopping.collectAsState()
    val activeInviteCode by viewModel.activeInviteCode.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val syncStats by viewModel.syncStats.collectAsState()

    if (wedding == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = RoyalMaroon,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Welcome to Marwadi Wedding Planner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroonDark
                )
                Text(
                    text = "Create or select a wedding to begin organizing rituals, guests, hotel accommodations, budget, and vendors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                androidx.compose.material3.Button(
                    onClick = { onNavigate("wedding_home") },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select / Create Wedding", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val currentW = wedding!!
    val totalBudget = currentW.overallBudget.coerceAtLeast(1.0)
    val remainingBudget = maxOf(0.0, currentW.overallBudget - totalExpense)
    val budgetPercent = ((totalExpense / totalBudget) * 100).toInt()

    val completedRituals = rituals.count { it.status == "COMPLETED" }
    val totalRituals = rituals.size.coerceAtLeast(1)
    val ritualProgress = (completedRituals.toFloat() / totalRituals)
    val nextRitual = rituals.firstOrNull { it.status != "COMPLETED" }

    val pendingTasks = tasks.count { it.status != "COMPLETED" }
    val highPriorityTasks = tasks.count { it.priority == "HIGH" && it.status != "COMPLETED" }
    val pendingShopping = consolidatedShopping.count { !it.isFullyPurchased }
    val pendingVendorPayments = vendors.sumOf { maxOf(0.0, it.totalContractValue - it.advancePaid) }
    val totalGuestsHeadcount = guests.sumOf { it.numberOfMembers }
    val confirmedGuests = guests.count { it.rsvpStatus == "ACCEPTED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Bento Hero Countdown Block
        item {
            CountdownBanner(
                brideName = currentW.brideName,
                groomName = currentW.groomName,
                weddingDate = currentW.weddingDate,
                venue = currentW.venue,
                city = currentW.city
            )
        }

        // Family Cloud Sync & Multi-User Collab Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("cloud_sync") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RoyalMaroon.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = RoyalMaroon,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Family Cloud Collaboration",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroonDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = RoyalGoldLight.copy(alpha = 0.4f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, RoyalGold)
                                ) {
                                    Text(
                                        text = activeInviteCode ?: "SYNC",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = RoyalMaroonDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Live sync with family • Tap to share invite code or sign in",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open Cloud Sync",
                        tint = RoyalGoldDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Budget Alert if high utilization
        if (budgetPercent >= 80) {
            item {
                Surface(
                    color = if (budgetPercent >= 100) ErrorCrimsonLight else WarningAmberLight,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (budgetPercent >= 100) ErrorCrimson else WarningAmber
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (budgetPercent >= 100) ErrorCrimson else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (budgetPercent >= 100)
                                "Budget Alert: Expenses exceeded overall limit by ${budgetPercent - 100}%!"
                            else
                                "Notice: $budgetPercent% of total wedding budget has been utilized.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (budgetPercent >= 100) ErrorCrimson else WarningAmber
                        )
                    }
                }
            }
        }

        // 2. Bento Asymmetric Row: Left = Budget Utilization, Right = Rituals (Vibrant Gold)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Budget Bento Tile (White Card)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                        .clickable { onNavigate("expenses") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RoyalMaroon.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = RoyalMaroon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Surface(
                                color = if (budgetPercent > 90) ErrorCrimsonLight else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$budgetPercent% USED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (budgetPercent > 90) ErrorCrimson else BentoSlateMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "TOTAL BUDGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlateMuted,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatCurrency(currentW.overallBudget),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        MarwadiProgressBar(
                            progress = totalExpense.toFloat() / totalBudget.toFloat(),
                            height = 6.dp,
                            barColor = if (budgetPercent > 90) ErrorCrimson else RoyalGoldDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Spent: ${formatCurrency(totalExpense)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoSlateMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Rituals Bento Tile (Vibrant Saffron Gold Card)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BentoGold, RoundedCornerShape(24.dp))
                        .clickable { onNavigate("rituals") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RoyalMaroon.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = RoyalMaroon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Surface(
                                color = RoyalMaroon,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${(ritualProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "MARWADI RITUALS",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoyalMaroonDark.copy(alpha = 0.8f),
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$completedRituals / ${rituals.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalMaroonDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (nextRitual != null) "Next: ${nextRitual.name}" else "All rituals completed!",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoyalMaroonDark.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Upcoming Ritual Bento Spotlight (Full Width 2-Column Span)
        if (nextRitual != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
                        .clickable { onNavigate("rituals") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = RoyalMaroon,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "UPCOMING VIDHI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = nextRitual.priority + " PRIORITY",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoSlateMuted
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = RoyalMaroon,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (nextRitual.hindiName.isNotEmpty()) "${nextRitual.name} (${nextRitual.hindiName})" else nextRitual.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (nextRitual.culturalSignificance.isNotEmpty()) nextRitual.culturalSignificance else if (nextRitual.description.isNotEmpty()) nextRitual.description else "Essential traditional Marwadi ceremony with holy mantras and family vidhi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoSlateMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = RoyalMaroon,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Checklist Tasks",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoSlate
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = RoyalGoldDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Puja Samagri",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Operations Bento Grid (4 Core Tiles)
        item {
            RoyalSectionHeader(
                title = "Operations & Coordination",
                actionText = "All Modules",
                onActionClick = { onNavigate("rituals") }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Tasks & Guests
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoStatTile(
                        title = "Pending Tasks",
                        value = "$pendingTasks",
                        subtitle = if (highPriorityTasks > 0) "$highPriorityTasks High Priority" else "On Track",
                        icon = Icons.Default.Assignment,
                        iconTint = SaffronOrange,
                        alert = highPriorityTasks > 0,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("tasks") }
                    )
                    BentoStatTile(
                        title = "Guests Headcount",
                        value = "$totalGuestsHeadcount",
                        subtitle = "$confirmedGuests Confirmed",
                        icon = Icons.Default.Groups,
                        iconTint = InfoBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("guests") }
                    )
                }

                // Row 2: Vendor Dues & Shopping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoStatTile(
                        title = "Vendor Dues",
                        value = formatCurrency(pendingVendorPayments),
                        subtitle = "${vendors.size} Contracts",
                        icon = Icons.Default.Store,
                        iconTint = RoyalMaroon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("vendors") }
                    )
                    BentoStatTile(
                        title = "Shopping Items",
                        value = "$pendingShopping Items",
                        subtitle = "${consolidatedShopping.size} Total Required",
                        icon = Icons.Default.ShoppingCart,
                        iconTint = RoyalGoldDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("shopping") }
                    )
                }
            }
        }

        // Quick Actions Bento Row
        item {
            RoyalSectionHeader(title = "Quick Actions")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = "+ Expense",
                    color = RoyalMaroon,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("expenses") }
                )
                QuickActionChip(
                    label = "+ Guest",
                    color = InfoBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("guests") }
                )
                QuickActionChip(
                    label = "+ Task",
                    color = SaffronOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("tasks") }
                )
                QuickActionChip(
                    label = "+ Puja Item",
                    color = RoyalGoldDark,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("shopping") }
                )
            }
        }

        // 5. Expense Breakdown Bento Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EXPENSE DISTRIBUTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoSlateMuted,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "By Ceremony & Vendor Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoyalMaroon.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = RoyalMaroon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (expenseByCategory.isEmpty()) {
                        Text(
                            text = "No expenses recorded yet. Add wedding expenses to see category breakdown.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoSlateMuted
                        )
                    } else {
                        SimpleDonutChart(data = expenseByCategory)
                    }
                }
            }
        }

        // 6. Management Modules Bento Grid
        item {
            RoyalSectionHeader(title = "Wedding Suite")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoModuleTile(
                        title = "26 Rituals",
                        subtitle = "Vidhis & Mantras",
                        icon = Icons.Default.Celebration,
                        color = RoyalMaroon,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("rituals") }
                    )
                    BentoModuleTile(
                        title = "Expenses",
                        subtitle = "Paid By & Budgets",
                        icon = Icons.Default.ReceiptLong,
                        color = SaffronOrange,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("expenses") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoModuleTile(
                        title = "Shopping",
                        subtitle = "Puja & Clothing",
                        icon = Icons.Default.ShoppingCart,
                        color = RoyalGoldDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("shopping") }
                    )
                    BentoModuleTile(
                        title = "Inventory",
                        subtitle = "Item Possession",
                        icon = Icons.Default.Inventory,
                        color = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("shopping") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoModuleTile(
                        title = "Guests & RSVP",
                        subtitle = "Stay & Transit",
                        icon = Icons.Default.Groups,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("guests") }
                    )
                    BentoModuleTile(
                        title = "Family Roles",
                        subtitle = "Gotra & Duties",
                        icon = Icons.Default.People,
                        color = Color(0xFF9333EA),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("family") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoModuleTile(
                        title = "Vendors",
                        subtitle = "Quotes & Advance",
                        icon = Icons.Default.Store,
                        color = Color(0xFFDB2777),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("vendors") }
                    )
                    BentoModuleTile(
                        title = "Timeline",
                        subtitle = "Muhurat Calendar",
                        icon = Icons.Default.CalendarMonth,
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("calendar") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoModuleTile(
                        title = "Shagun & Gifts",
                        subtitle = "Received & Given",
                        icon = Icons.Default.CardGiftcard,
                        color = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("gifts") }
                    )
                    BentoModuleTile(
                        title = "Reports & Audit",
                        subtitle = "Financial Summary",
                        icon = Icons.Default.Assessment,
                        color = RoyalMaroonDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("reports") }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun BentoStatTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    alert: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (alert) {
                    Surface(
                        color = ErrorCrimsonLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PRIORITY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorCrimson,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BentoSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = BentoSlateMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (alert) ErrorCrimson else BentoSlateMuted,
                fontWeight = if (alert) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BentoModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoSlateMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

