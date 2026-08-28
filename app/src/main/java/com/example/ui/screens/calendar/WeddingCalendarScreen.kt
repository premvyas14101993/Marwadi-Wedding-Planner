package com.example.ui.screens.calendar

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDate
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.CalendarEventItem
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun WeddingCalendarScreen(
    viewModel: WeddingViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.calendarEvents.collectAsState()
    val currentWedding by viewModel.currentWedding.collectAsState()

    val groupedEvents = events.groupBy { formatDate(it.date) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Auspicious Wedding Timeline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroonDark
                    )
                    Text(
                        text = "Chronological itinerary combining Rituals, Preparation Tasks, and Vendor Due Dates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (events.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Timeline Events",
                    message = "Set dates for rituals or tasks to populate the master wedding calendar.",
                    icon = Icons.Default.CalendarMonth
                )
            }
        } else {
            groupedEvents.forEach { (dateStr, dayEvents) ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RoyalMaroon)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    }
                }

                items(dayEvents, key = { it.id }) { event ->
                    CalendarEventCard(event = event)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun CalendarEventCard(
    event: CalendarEventItem,
    modifier: Modifier = Modifier
) {
    val (typeColor, typeIcon, typeLabel) = when (event.type) {
        "RITUAL" -> Triple(RoyalMaroon, Icons.Default.Celebration, "Marwadi Ritual")
        "TASK" -> Triple(SaffronOrange, Icons.Default.Assignment, "Action Task")
        else -> Triple(InfoBlue, Icons.Default.Payment, "Vendor Payment")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderGold, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${event.subtitle} • $typeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusBadge(status = event.status)
        }
    }
}
