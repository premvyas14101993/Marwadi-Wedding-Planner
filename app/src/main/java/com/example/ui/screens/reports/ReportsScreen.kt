package com.example.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MarwadiProgressBar
import com.example.ui.components.RoyalCard
import com.example.ui.components.RoyalSectionHeader
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WeddingViewModel
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val wedding by viewModel.currentWedding.collectAsState()
    val rituals by viewModel.rituals.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val expenseByPerson by viewModel.expenseByPerson.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val guests by viewModel.guests.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    if (wedding == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No wedding selected", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val currentW = wedding!!
    val totalBudget = currentW.overallBudget
    val remaining = maxOf(0.0, totalBudget - totalExpense)
    val completedRituals = rituals.count { it.status == "COMPLETED" }
    val confirmedGuests = guests.filter { it.rsvpStatus == "ACCEPTED" }.sumOf { it.numberOfMembers }
    val totalGuests = guests.sumOf { it.numberOfMembers }
    val totalVendorContracts = vendors.sumOf { it.totalContractValue }
    val totalVendorAdvance = vendors.sumOf { it.advancePaid }
    val totalVendorPending = maxOf(0.0, totalVendorContracts - totalVendorAdvance)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wedding Executive Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroonDark
                    )
                    Text(
                        text = "${currentW.name} • ${formatDate(currentW.weddingDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalMaroon
                    )
                    Text(
                        text = "Detailed audit summary of budget utilization, family contributions, vendor dues, and ritual readiness.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Budget & Financial Audit Card
        item {
            RoyalCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Financial Audit Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = RoyalMaroon)
                    }

                    HorizontalDivider(color = CardBorderGold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Sanctioned Overall Budget:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCurrency(totalBudget), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Expenses Incurred:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCurrency(totalExpense), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Net Remaining Balance:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCurrency(remaining), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (remaining > 0) SuccessGreen else ErrorCrimson)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    val utilPct = if (totalBudget > 0) (totalExpense / totalBudget).toFloat() else 0f
                    MarwadiProgressBar(progress = utilPct)
                    Text(
                        text = "Budget Utilization: ${(utilPct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Family Contribution Breakdown ("Paid By")
        if (expenseByPerson.isNotEmpty()) {
            item {
                RoyalCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Family Contributions (Paid By Settlement)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = CardBorderGold)

                        expenseByPerson.forEach { (person, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = person, style = MaterialTheme.typography.bodyMedium)
                                Text(text = formatCurrency(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                            }
                        }
                    }
                }
            }
        }

        // Vendor Payables Summary
        item {
            RoyalCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Vendor Contracts & Outstanding Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = CardBorderGold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Vendor Contracts:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCurrency(totalVendorContracts), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Advances Paid:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCurrency(totalVendorAdvance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Pending Vendor Dues:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCurrency(totalVendorPending), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                    }
                }
            }
        }

        // Guest & Attendance Summary
        item {
            RoyalCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Guest Attendance & Logistics Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = CardBorderGold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Invited Headcount:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$totalGuests Guests (${guests.size} Families)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Confirmed RSVP Attendance:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$confirmedGuests Guests", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Hotel Accommodations Requested:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${guests.count { it.accommodationRequired }} Families", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Share & Backup Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val shareText = """
                            Royal Marwadi Wedding Summary: ${currentW.name}
                            Groom: ${currentW.groomName} | Bride: ${currentW.brideName}
                            Date: ${formatDate(currentW.weddingDate)} | Venue: ${currentW.venue}, ${currentW.city}
                            
                            Budget: ${formatCurrency(totalBudget)}
                            Total Spent: ${formatCurrency(totalExpense)}
                            Remaining: ${formatCurrency(remaining)}
                            
                            Rituals: $completedRituals / ${rituals.size} Completed
                            Confirmed Guests: $confirmedGuests / $totalGuests
                            Pending Vendor Dues: ${formatCurrency(totalVendorPending)}
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Wedding Report"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Share Wedding Report", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
