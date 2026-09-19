package com.ascend75.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendShapesTokens
import com.ascend75.core.designsystem.theme.AscendTheme
import com.ascend75.core.designsystem.theme.AscendTypography

enum class AscendButtonVariant {
    Primary,
    Secondary,
    Outline
}

@Composable
fun AscendButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AscendButtonVariant = AscendButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    val clickAction = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }

    when (variant) {
        AscendButtonVariant.Primary -> {
            Button(
                onClick = clickAction,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(AscendShapesTokens.Pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AscendPalette.PrimaryContainer,
                    contentColor = AscendPalette.OnPrimaryContainer,
                    disabledContainerColor = AscendPalette.SurfaceContainerHigh,
                    disabledContentColor = AscendPalette.OnSurfaceVariant.copy(alpha = 0.38f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = AscendTypography.labelLarge
                )
            }
        }
        AscendButtonVariant.Secondary -> {
            Button(
                onClick = clickAction,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(AscendShapesTokens.Pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AscendPalette.SurfaceContainerHigh,
                    contentColor = AscendPalette.OnSurface,
                    disabledContainerColor = AscendPalette.SurfaceContainerLow,
                    disabledContentColor = AscendPalette.OnSurfaceVariant.copy(alpha = 0.38f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = AscendTypography.labelLarge
                )
            }
        }
        AscendButtonVariant.Outline -> {
            OutlinedButton(
                onClick = clickAction,
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(AscendShapesTokens.Pill),
                border = BorderStroke(
                    1.dp,
                    if (enabled) AscendPalette.Outline.copy(alpha = 0.4f)
                    else AscendPalette.OutlineVariant.copy(alpha = 0.2f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AscendPalette.Primary,
                    disabledContentColor = AscendPalette.OnSurfaceVariant.copy(alpha = 0.38f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = AscendTypography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F131C)
@Composable
private fun AscendButtonPreview() {
    AscendTheme {
        AscendButton(
            text = "Begin Day 1",
            onClick = {}
        )
    }
}
