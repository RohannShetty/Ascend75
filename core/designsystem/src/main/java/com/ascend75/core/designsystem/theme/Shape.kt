package com.ascend75.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape & Corner Radius hierarchy mapped from DESIGN.md
 */
object AscendShapesTokens {
    val Small = 4.dp
    val Medium = 8.dp
    val Large = 12.dp
    val ExtraLarge = 16.dp
    val ContainerLarge = 24.dp
    val Pill = 9999.dp
}

val AscendShapes = Shapes(
    extraSmall = RoundedCornerShape(AscendShapesTokens.Small),
    small = RoundedCornerShape(AscendShapesTokens.Medium),
    medium = RoundedCornerShape(AscendShapesTokens.Large),
    large = RoundedCornerShape(AscendShapesTokens.ExtraLarge),
    extraLarge = RoundedCornerShape(AscendShapesTokens.ContainerLarge)
)
