package com.ascend75.feature.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography

@Composable
fun ReadingScreen(
    taskId: String,
    viewModel: ReadingViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column {
                Text(
                    text = "COGNITIVE GROWTH",
                    style = AscendTypography.labelSmall,
                    color = AscendPalette.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "10 Pages Non-Fiction",
                    style = AscendTypography.headlineMedium,
                    color = AscendPalette.OnSurface
                )
            }

            // Book Title Input
            Column {
                Text(text = "Current Book Title", style = AscendTypography.labelMedium, color = AscendPalette.OnSurface)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.bookTitle,
                    onValueChange = viewModel::setBookTitle,
                    placeholder = {
                        Text("e.g. Atomic Habits or Deep Work", color = AscendPalette.OnSurfaceVariant.copy(alpha = 0.5f))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AscendPalette.Primary,
                        unfocusedBorderColor = AscendPalette.Outline.copy(alpha = 0.3f)
                    )
                )
            }

            // Page Range Counters
            GlassCard {
                Column {
                    Text(text = "PAGE LOGGING", style = AscendTypography.labelSmall, color = AscendPalette.Primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.startPage.toString(),
                            onValueChange = {
                                val start = it.toIntOrNull() ?: 0
                                viewModel.setPages(start, state.endPage)
                            },
                            label = { Text("Start Page") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.endPage.toString(),
                            onValueChange = {
                                val end = it.toIntOrNull() ?: 0
                                viewModel.setPages(state.startPage, end)
                            },
                            label = { Text("End Page") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total Pages: ${state.pagesRead} • ${if (state.pagesRead >= 10) "Target Met ✓" else "Need at least 10"}",
                        style = AscendTypography.bodyMedium,
                        color = if (state.pagesRead >= 10) AscendPalette.Success else AscendPalette.Warning
                    )
                }
            }

            // Reflection Takeaway
            Column {
                Text(text = "Key Takeaway Reflection", style = AscendTypography.labelMedium, color = AscendPalette.OnSurface)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.keyTakeaway,
                    onValueChange = viewModel::setTakeaway,
                    placeholder = {
                        Text("What single idea or mental model did you extract today?", color = AscendPalette.OnSurfaceVariant.copy(alpha = 0.5f))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AscendPalette.Primary,
                        unfocusedBorderColor = AscendPalette.Outline.copy(alpha = 0.3f)
                    )
                )
            }

            state.errorMessage?.let { errorMsg ->
                Text(text = errorMsg, color = AscendPalette.Error, style = AscendTypography.bodySmall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            AscendButton(
                text = "Save Reading Session",
                variant = AscendButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.saveSession(taskId, onFinished) }
            )
        }
    }
}
