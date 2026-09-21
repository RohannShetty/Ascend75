package com.ascend75.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTheme
import com.ascend75.core.designsystem.theme.AscendTypography

/**
 * Hero completion indicator: one arc per habit, filled from a single animated progress float so the
 * whole ring advances together with a single spring.
 */
@Composable
fun AscendProgressRing(
    totalSegments: Int,
    completedSegments: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    strokeWidth: Dp = 12.dp
) {
    val progressFraction = if (totalSegments > 0) {
        (completedSegments.toFloat() / totalSegments.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 180f),
        label = "progressAnimation"
    )

    val activeBrush = remember {
        Brush.sweepGradient(listOf(AscendPalette.PrimaryContainer, AscendPalette.Primary))
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val segmentCount = totalSegments.coerceAtLeast(1)
            val sweepPerSegment = 360f / segmentCount
            val gap = if (sweepPerSegment > 12f) 6f else 0f

            for (index in 0 until segmentCount) {
                val startAngle = -90f + index * sweepPerSegment
                val segmentSweep = sweepPerSegment - gap

                drawArc(
                    color = AscendPalette.SurfaceContainerHighest,
                    startAngle = startAngle,
                    sweepAngle = segmentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                val segmentFill = (animatedProgress * segmentCount - index).coerceIn(0f, 1f)
                if (segmentFill > 0f) {
                    drawArc(
                        brush = activeBrush,
                        startAngle = startAngle,
                        sweepAngle = segmentSweep * segmentFill,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progressFraction * 100).toInt()}%",
                style = AscendTypography.headlineLarge,
                color = AscendPalette.OnSurface
            )
            Text(
                text = "$completedSegments of $totalSegments done",
                style = AscendTypography.bodySmall,
                color = AscendPalette.OnSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F131C)
@Composable
private fun AscendProgressRingPreview() {
    AscendTheme {
        AscendProgressRing(
            totalSegments = 6,
            completedSegments = 4
        )
    }
}
