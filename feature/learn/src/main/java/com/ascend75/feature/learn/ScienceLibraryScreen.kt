package com.ascend75.feature.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.designsystem.components.AscendLoadingGate
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

private val StudyCategories = listOf("ALL", "CIRCADIAN", "FOCUS", "DOPAMINE", "RECOVERY", "HABITS")

@Composable
fun ScienceLibraryScreen(
    viewModel: ScienceLibraryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCard = state.selectedCard

    BackHandler(enabled = selectedCard != null) {
        viewModel.selectCard(null)
    }

    if (selectedCard != null) {
        ScienceCardDetailScreen(
            card = selectedCard,
            onBack = { viewModel.selectCard(null) },
            onToggleBookmark = { viewModel.toggleBookmark(selectedCard) },
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        if (state.isLoading) {
            AscendLoadingGate(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = "BEHAVIORAL SCIENCE",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Text(
                    text = "75-Day Curriculum",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
                Text(
                    text = "Unlocked up to Day ${state.currentDay} of 75",
                    style = AscendTypography.bodySmall,
                    color = AscendPalette.OnSurfaceVariant
                )
            }

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AscendPalette.OnSurfaceVariant
                    )
                },
                placeholder = {
                    Text(
                        text = "Search circadian, dopamine, protocols...",
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AscendPalette.Primary,
                    unfocusedBorderColor = AscendPalette.Outline.copy(alpha = 0.3f)
                )
            )

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(StudyCategories, key = { it }) { cat ->
                    val isSelected = state.selectedCategory.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AscendPalette.PrimaryContainer else AscendPalette.SurfaceContainerHigh)
                            .clickable { viewModel.setSelectedCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = AscendTypography.labelSmall,
                            color = if (isSelected) AscendPalette.OnPrimaryContainer else AscendPalette.OnSurfaceVariant
                        )
                    }
                }
            }

            // List of Unlocked Cards
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.filteredCards, key = { it.dayNumber }) { card ->
                    GlassCard(
                        onClick = { viewModel.selectCard(card) }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DAY ${card.dayNumber} • ${card.category}",
                                    style = AscendTypography.labelSmall,
                                    color = AscendPalette.Primary
                                )
                                IconButton(onClick = { viewModel.toggleBookmark(card) }) {
                                    Icon(
                                        imageVector = if (card.isBookmarked) {
                                            Icons.Filled.Bookmark
                                        } else {
                                            Icons.Outlined.BookmarkBorder
                                        },
                                        contentDescription = if (card.isBookmarked) {
                                            "Remove bookmark"
                                        } else {
                                            "Bookmark day ${card.dayNumber}"
                                        },
                                        tint = if (card.isBookmarked) AscendPalette.Primary else AscendPalette.OnSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.title,
                                style = AscendTypography.headlineSmall,
                                color = AscendPalette.OnSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.summary,
                                style = AscendTypography.bodySmall,
                                color = AscendPalette.OnSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
