package com.ascend75.feature.photos

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascend75.core.designsystem.components.AscendButton
import com.ascend75.core.designsystem.components.AscendButtonVariant
import com.ascend75.core.designsystem.components.GlassCard
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

private const val THUMBNAIL_TARGET_PX = 400

@Composable
fun PhotoVaultScreen(
    viewModel: PhotoVaultViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AscendPalette.Background
    ) { padding ->
        if (state.isCapturing) {
            CameraCaptureView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onCaptured = { bytes ->
                    val taskId = state.taskId
                    if (taskId == null) {
                        viewModel.onCaptureError("No progress-photo task exists for this day.")
                        bytes.fill(0)
                    } else {
                        viewModel.saveCapturedPhoto(taskId, state.dayNumber, bytes) { }
                    }
                },
                onError = { message -> viewModel.onCaptureError(message) },
                onCancel = { viewModel.cancelCapture() }
            )
            return@Scaffold
        }

        if (state.isLocked) {
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

                state.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.Error
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AscendButton(
                    text = if (state.isBiometricEnabled) "Unlock with Biometrics / PIN" else "Unlock Vault",
                    variant = AscendButtonVariant.Primary,
                    onClick = {
                        val activity = context.findFragmentActivity()
                        if (activity == null) {
                            viewModel.unlockVault()
                        } else {
                            viewModel.requestUnlock(activity)
                        }
                    }
                )
            }
        } else {
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

                if (state.captureCompleted) {
                    GlassCard(
                        containerColor = AscendPalette.Success.copy(alpha = 0.12f),
                        borderColor = AscendPalette.Success.copy(alpha = 0.4f)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Progress photo secured",
                                style = AscendTypography.headlineSmall,
                                color = AscendPalette.OnSurface
                            )
                            Text(
                                text = "Encrypted with the hardware-backed vault key. Nothing left the device.",
                                style = AscendTypography.bodySmall,
                                color = AscendPalette.OnSurfaceVariant
                            )
                            AscendButton(
                                text = "Done",
                                variant = AscendButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.acknowledgeCapture()
                                    onFinished()
                                }
                            )
                        }
                    }
                }

                BeforeAfterCard(
                    viewModel = viewModel,
                    photos = state.photos,
                    committedFraction = state.splitSliderPosition
                )

                AscendButton(
                    text = "Capture Day ${state.dayNumber} Photo",
                    variant = AscendButtonVariant.Primary,
                    enabled = state.taskId != null,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    onClick = { viewModel.startCapture() }
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.Error
                    )
                }

                Text(
                    text = "Historical Photo Log",
                    style = AscendTypography.labelLarge,
                    color = AscendPalette.OnSurface
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.photos, key = { photo -> photo.filePath }) { photo ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            VaultThumbnail(
                                filePath = photo.filePath,
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(AscendPalette.SurfaceContainerLowest.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = "Day ${photo.dayNumber}",
                                    style = AscendTypography.labelSmall,
                                    color = AscendPalette.OnSurface,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BeforeAfterCard(
    viewModel: PhotoVaultViewModel,
    photos: List<VaultPhotoItem>,
    committedFraction: Float
) {
    val beforePath = remember(photos) { photos.minByOrNull { it.dayNumber }?.filePath }
    val afterPath = remember(photos) { photos.maxByOrNull { it.dayNumber }?.filePath }

    // Held locally so a drag frame recomposes only this card instead of the whole screen and grid.
    var sliderFraction by remember { mutableFloatStateOf(committedFraction) }

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
                    .background(AscendPalette.SurfaceContainerLowest),
                contentAlignment = Alignment.Center
            ) {
                if (afterPath != null) {
                    VaultThumbnail(
                        filePath = afterPath,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (beforePath != null && beforePath != afterPath) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                clipRect(right = size.width * sliderFraction) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    ) {
                        VaultThumbnail(
                            filePath = beforePath,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (beforePath == null && afterPath == null) {
                    Text(
                        text = "No progress photos yet",
                        style = AscendTypography.bodySmall,
                        color = AscendPalette.OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = sliderFraction,
                onValueChange = { sliderFraction = it },
                onValueChangeFinished = { viewModel.setSplitSliderPosition(sliderFraction) },
                colors = SliderDefaults.colors(
                    thumbColor = AscendPalette.Primary,
                    activeTrackColor = AscendPalette.Primary
                )
            )
        }
    }
}

@Composable
private fun VaultThumbnail(
    filePath: String,
    viewModel: PhotoVaultViewModel,
    modifier: Modifier = Modifier
) {
    val image by produceState<ImageBitmap?>(initialValue = null, filePath) {
        value = decodeScaledThumbnail(viewModel.readPhotoBytes(filePath))
    }

    Box(
        modifier = modifier.background(AscendPalette.SurfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = image
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "Encrypted",
                style = AscendTypography.labelSmall,
                color = AscendPalette.OnSurfaceVariant
            )
        }
    }
}

/** Decodes at a bounded sample size and zeroes the decrypted plaintext afterwards. */
private suspend fun decodeScaledThumbnail(bytes: ByteArray?): ImageBitmap? {
    if (bytes == null || bytes.isEmpty()) return null
    return withContext(Dispatchers.Default) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

            var inSampleSize = 1
            while (bounds.outWidth / (inSampleSize * 2) >= THUMBNAIL_TARGET_PX &&
                bounds.outHeight / (inSampleSize * 2) >= THUMBNAIL_TARGET_PX
            ) {
                inSampleSize *= 2
            }

            val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
        } finally {
            bytes.fill(0)
        }
    }
}

@Composable
private fun CameraCaptureView(
    onCaptured: (ByteArray) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(hasPermission) {
        if (!hasPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(lifecycleOwner, hasPermission) {
        if (hasPermission) {
            controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            controller.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            controller.bindToLifecycle(lifecycleOwner)
        }
        onDispose {
            runCatching { controller.unbind() }
        }
    }

    Box(modifier = modifier.background(AscendPalette.Background)) {
        if (hasPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).apply { this.controller = controller }
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera permission is required to capture a progress photo.",
                    style = AscendTypography.bodyMedium,
                    color = AscendPalette.OnSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AscendButton(
                text = "Cancel",
                variant = AscendButtonVariant.Secondary,
                onClick = onCancel
            )

            IconButton(
                enabled = hasPermission,
                onClick = {
                    controller.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bytes = image.planes.first().buffer.toByteArrayExact()
                                image.close()
                                onCaptured(bytes)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception.localizedMessage ?: "Photo capture failed.")
                            }
                        }
                    )
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AscendPalette.Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Capture",
                    tint = AscendPalette.OnPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun ByteBuffer.toByteArrayExact(): ByteArray {
    val bytes = ByteArray(remaining())
    get(bytes)
    return bytes
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
