package com.ascend75.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapesTokens
import com.ascend75.core.designsystem.theme.AscendTypography

/**
 * Branded first-frame placeholder shown while the persisted preferences are being read.
 *
 * Skeleton bars mirror the dashboard's header / ring / list heights so the first real frame does not
 * shift the layout when it arrives.
 */
@Composable
fun AscendLoadingGate(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp).padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = AscendPalette.Primary,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "ASCEND 75",
            style = AscendTypography.labelSmall,
            color = AscendPalette.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        LoadingSkeletonBar(height = 96.dp)
        LoadingSkeletonBar(height = 200.dp)
        LoadingSkeletonBar(height = 72.dp)
    }
}

@Composable
private fun LoadingSkeletonBar(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(AscendShapesTokens.ExtraLarge))
            .background(AscendPalette.SurfaceContainerLow)
    )
}
