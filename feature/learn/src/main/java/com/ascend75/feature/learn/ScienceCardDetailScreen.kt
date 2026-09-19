package com.ascend75.feature.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascend75.core.database.entities.ScienceCardEntity
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun ScienceCardDetailScreen(
    card: ScienceCardEntity,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AscendPalette.OnSurface
                    )
                }
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (card.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (card.isBookmarked) AscendPalette.Primary else AscendPalette.OnSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = card.category.uppercase(),
                style = AscendTypography.labelSmall,
                color = AscendPalette.Primary
            )
            Text(
                text = card.title,
                style = AscendTypography.headlineMedium,
                color = AscendPalette.OnSurface
            )

            // Core Takeaway Summary Card
            GlassCard(containerColor = AscendPalette.SurfaceContainerHigh) {
                Column {
                    Text(
                        text = "CORE EVIDENCE TAKEAWAY",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.Secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = card.summary,
                        style = AscendTypography.bodyLarge,
                        color = AscendPalette.OnSurface
                    )
                }
            }

            // Biological Mechanism
            GlassCard {
                Column {
                    Text(
                        text = "BIOLOGICAL & PHYSIOLOGICAL MECHANISM",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.Primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = card.mechanism,
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurfaceVariant
                    )
                }
            }

            // Practical Daily Action Step
            GlassCard(borderColor = AscendPalette.Success.copy(alpha = 0.3f)) {
                Column {
                    Text(
                        text = "PRACTICAL IMPLEMENTATION STEP",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.Success
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = card.actionItem,
                        style = AscendTypography.bodyMedium,
                        color = AscendPalette.OnSurface
                    )
                }
            }

            // Primary Peer-Reviewed Citation
            GlassCard {
                Column {
                    Text(
                        text = "PRIMARY PEER-REVIEWED CITATION",
                        style = AscendTypography.labelSmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.sourceCitation,
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurface
                    )
                    card.doiOrUrl?.let { doi ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = doi,
                            style = AscendTypography.bodySmall,
                            color = AscendPalette.Secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
