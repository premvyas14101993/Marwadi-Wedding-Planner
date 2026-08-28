package com.example.ui.screens.search

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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.RoyalMaroonDark
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SearchMatchItem
import com.example.ui.viewmodel.WeddingViewModel

@Composable
fun GlobalSearchScreen(
    viewModel: WeddingViewModel,
    onNavigateToRitual: (Long) -> Unit,
    onNavigateToModule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search rituals, expenses, guests, vendors, tasks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalMaroon) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("global_search_bar")
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (searchQuery.isBlank()) {
            EmptyStateView(
                title = "Global Wedding Search",
                message = "Search across all rituals, expenses, guest lists, vendors, shopping samagri, and tasks instantly.",
                icon = Icons.Default.Search
            )
        } else if (searchResults.isEmpty()) {
            EmptyStateView(
                title = "No Matches Found",
                message = "No wedding items matched '$searchQuery'. Try checking spelling or search a different keyword.",
                icon = Icons.Default.Search
            )
        } else {
            Text(
                text = "${searchResults.size} matches found for \"$searchQuery\"",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults, key = { "${it.entityType}_${it.id}" }) { item ->
                    SearchResultCard(
                        item = item,
                        onClick = {
                            when (item.entityType) {
                                "RITUAL" -> onNavigateToRitual(item.id)
                                "EXPENSE" -> onNavigateToModule("expenses")
                                "GUEST" -> onNavigateToModule("guests")
                                "VENDOR" -> onNavigateToModule("vendors")
                                "TASK" -> onNavigateToModule("tasks")
                                "MATERIAL" -> onNavigateToModule("shopping")
                                "NOTE" -> onNavigateToModule("notes")
                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    item: SearchMatchItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (item.entityType) {
        "RITUAL" -> Pair(RoyalMaroon, Icons.Default.Celebration)
        "EXPENSE" -> Pair(SaffronOrange, Icons.Default.ReceiptLong)
        "GUEST" -> Pair(InfoBlue, Icons.Default.Groups)
        "VENDOR" -> Pair(Color(0xFFD81B60), Icons.Default.Store)
        "TASK" -> Pair(Color(0xFF5E35B1), Icons.Default.Assignment)
        "MATERIAL" -> Pair(RoyalGoldDark, Icons.Default.ShoppingBag)
        else -> Pair(Color(0xFF6D4C41), Icons.Default.Note)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderGold, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
