package com.ascend75.feature.photos

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun PhotoVaultScreen(
    taskId: String,
    dayNumber: Int,
    viewModel: PhotoVaultViewModel,
    onRequestBiometricAuth: () -> Unit,
    onLaunchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        if (state.isLocked) {
            // Obfuscated / Locked Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AscendPalette.SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        tint = AscendPalette.Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Encrypted Photo Vault",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your progress photos are stored with hardware-backed AES-256-GCM encryption in an app-private sandbox. Media is never exposed to public galleries.",
                    style = AscendTypography.bodyMedium,
                    color = AscendPalette.OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                AscendButton(
                    text = "Unlock with Biometrics / PIN",
                    variant = AscendButtonVariant.Primary,
                    onClick = onRequestBiometricAuth
                )
            }
        } else {
            // Unlocked Vault View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BIOMETRIC VAULT",
                            style = AscendTypography.labelSmall,
                            color = AscendPalette.Primary
                        )
                        Text(
                            text = "Transformation Timeline",
                            style = AscendTypography.headlineMedium,
                            color = AscendPalette.OnSurface
                        )
                    }

                    AscendButton(
                        text = "Lock",
                        variant = AscendButtonVariant.Secondary,
                        onClick = { viewModel.lockVault() }
                    )
                }

                // Interactive Split Slider (Day 1 vs Current Day)
                GlassCard {
                    Column {
                        Text(
                            text = "BEFORE & AFTER COMPARISON",
                            style = AscendTypography.labelSmall,
                            color = AscendPalette.Secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AscendPalette.SurfaceContainerLowest)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Day 1 side
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = state.splitSliderPosition)
                                        .background(AscendPalette.SurfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Day 1 Baseline",
                                        style = AscendTypography.bodySmall,
                                        color = AscendPalette.OnSurfaceVariant
                                    )
                                }

                                // Current Day side
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(AscendPalette.SurfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Day $dayNumber Current",
                                        style = AscendTypography.bodySmall,
                                        color = AscendPalette.Primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = state.splitSliderPosition,
                            onValueChange = viewModel::setSplitSliderPosition,
                            colors = SliderDefaults.colors(
                                thumbColor = AscendPalette.Primary,
                                activeTrackColor = AscendPalette.Primary
                            )
                        )
                    }
                }

                // Capture CTA Button
                AscendButton(
                    text = "Capture Day $dayNumber Photo",
                    variant = AscendButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    onClick = onLaunchCamera
                )

                Text(
                    text = "Historical Photo Log",
                    style = AscendTypography.labelLarge,
                    color = AscendPalette.OnSurface
                )

                // Grid of encrypted thumbnails
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.photos) { photoItem ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AscendPalette.SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Day ${photoItem.dayNumber}",
                                style = AscendTypography.bodySmall,
                                color = AscendPalette.OnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
