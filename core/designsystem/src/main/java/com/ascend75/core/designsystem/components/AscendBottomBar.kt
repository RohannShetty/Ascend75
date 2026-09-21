package com.ascend75.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

/**
 * The five destinations of the Ascend 75 information architecture.
 *
 * Deliberately holds no navigation route: this module has no navigation dependency, so the app
 * module owns the tab -> route mapping.
 */
enum class AscendTab(val label: String, val tabIcon: ImageVector) {
    TODAY("Today", Icons.Filled.Today),
    TRACKERS("Trackers", Icons.Filled.Checklist),
    LEARN("Learn", Icons.Filled.MenuBook),
    VAULT("Vault", Icons.Filled.Lock),
    MORE("More", Icons.Filled.MoreHoriz)
}

/**
 * Fixed bottom navigation bar shared by every non-onboarding destination.
 *
 * Renders with an alpha-tinted backdrop and a hairline separator only — no blur or render effect,
 * which would force an offscreen composite on every frame the bar is on screen.
 */
@Composable
fun AscendBottomBar(
    selected: AscendTab,
    onSelect: (AscendTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AscendPalette.SurfaceContainerLowest.copy(alpha = 0.94f))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AscendPalette.OutlineVariant.copy(alpha = 0.4f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AscendTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val tint = if (isSelected) AscendPalette.Primary else AscendPalette.OnSurfaceVariant
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            indication = null,
                            interactionSource = interactionSource,
                            onClick = { onSelect(tab) }
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.tabIcon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = AscendTypography.labelMedium,
                        color = tint,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
