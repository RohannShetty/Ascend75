package com.ascend75.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ThemeContrastTest {

    private fun linearizeComponent(colorComponent: Float): Double {
        return if (colorComponent <= 0.03928) {
            colorComponent / 12.92
        } else {
            ((colorComponent + 0.055) / 1.055).pow(2.4)
        }
    }

    private fun calculateLuminance(color: Color): Double {
        val r = linearizeComponent(color.red)
        val g = linearizeComponent(color.green)
        val b = linearizeComponent(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun verifyOnSurfaceMeetsWcagThresholdsAgainstSurface() {
        val contrast = calculateContrastRatio(AscendPalette.OnSurface, AscendPalette.Surface)
        // WCAG AA requires at least 4.5:1 for normal text, WCAG AAA requires 7.0:1
        assertTrue("OnSurface contrast ($contrast) should exceed 7.0:1 (AAA)", contrast >= 7.0)
    }

    @Test
    fun verifyOnPrimaryContrastAgainstPrimary() {
        val contrast = calculateContrastRatio(AscendPalette.OnPrimary, AscendPalette.Primary)
        assertTrue("OnPrimary contrast ($contrast) should exceed 4.5:1 (AA)", contrast >= 4.5)
    }

    @Test
    fun verifyOnPrimaryContainerContrast() {
        val contrast = calculateContrastRatio(AscendPalette.OnPrimaryContainer, AscendPalette.PrimaryContainer)
        assertTrue("OnPrimaryContainer contrast ($contrast) should exceed 4.5:1 (AA)", contrast >= 4.5)
    }

    @Test
    fun verifyOnSecondaryContrastAgainstSecondary() {
        val contrast = calculateContrastRatio(AscendPalette.OnSecondary, AscendPalette.Secondary)
        assertTrue("OnSecondary contrast ($contrast) should exceed 4.5:1 (AA)", contrast >= 4.5)
    }

    @Test
    fun verifyOnTertiaryContrastAgainstTertiary() {
        val contrast = calculateContrastRatio(AscendPalette.OnTertiary, AscendPalette.Tertiary)
        assertTrue("OnTertiary contrast ($contrast) should exceed 4.5:1 (AA)", contrast >= 4.5)
    }
}
