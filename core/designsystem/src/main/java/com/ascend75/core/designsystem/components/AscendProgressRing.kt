package com.ascend75.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTheme
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun AscendProgressRing(
    totalSegments: Int,
    completedSegments: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    strokeWidth: Dp = 14.dp,
    label: String = "TODAY"
) {
    val progressFraction = if (totalSegments > 0) {
        (completedSegments.toFloat() / totalSegments.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Draw Background Track Ring
            drawArc(
                color = AscendPalette.SurfaceContainerHighest,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Draw Active Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = AscendPalette.Primary,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Inner stats column
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
