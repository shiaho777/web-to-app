package com.webtoapp.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.IconLibraryStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * WeChat-avatar-style icon cropper: the user drags / pinch-zooms the picked
 * image under a fixed crop window. The visible image (never the raw gesture
 * math) is what gets exported, so all crop math happens in preview
 * coordinates and maps back through one uniform scale at save time.
 */
enum class IconCropMode(val aspectRatio: Float?) {
    SQUARE(1f),
    FREE(null),
    CIRCLE(1f)
}

private const val CROP_MAX_DIMENSION = 2048
private const val CROP_OUTPUT_SIZE = 512

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconCropDialog(
    imageUri: Uri,
    onCropComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCropping by remember { mutableStateOf(false) }
    var cropMode by remember { mutableStateOf(IconCropMode.SQUARE) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }

    // Image transform in preview coordinates: uniform scale around the box
    // center plus a pan offset from that centered position.
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(imageUri) {
        isLoading = true
        errorMessage = null
        withContext(Dispatchers.IO) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(bounds, CROP_MAX_DIMENSION, CROP_MAX_DIMENSION)
                }
                val bitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
                if (bitmap != null) {
                    originalBitmap = bitmap
                } else {
                    errorMessage = Strings.cannotParseImage
                }
            } catch (e: Exception) {
                errorMessage = Strings.loadImageFailed.format(e.message)
            }
        }
        isLoading = false
    }

    val cropRect = remember(previewSize, cropMode) {
        val w = previewSize.width.toFloat()
        val h = previewSize.height.toFloat()
        if (w <= 0f || h <= 0f) {
            Rect.Zero
        } else {
            when (cropMode.aspectRatio) {
                null -> Rect(Offset.Zero, Size(w, h))
                else -> if (w <= h) {
                    Rect(Offset(0f, (h - w) / 2f), Size(w, w))
                } else {
                    Rect(Offset((w - h) / 2f, 0f), Size(h, h))
                }
            }
        }
    }

    // Minimum scale keeps the image covering the crop window (no gaps);
    // maximum is a generous zoom-in cap.
    val minScale = remember(originalBitmap, cropRect) {
        val bitmap = originalBitmap
        if (bitmap == null || cropRect == Rect.Zero || bitmap.width == 0 || bitmap.height == 0) {
            1f
        } else {
            max(
                cropRect.width / bitmap.width,
                cropRect.height / bitmap.height
            )
        }
    }
    val maxScale = remember(minScale) { (minScale * 8f).coerceAtLeast(1f) }

    LaunchedEffect(originalBitmap, cropMode, minScale) {
        if (originalBitmap != null && cropRect != Rect.Zero) {
            scale = minScale
            offsetX = 0f
            offsetY = 0f
        }
    }

    com.webtoapp.ui.design.WtaAlertDialog(
        onDismissRequest = { if (!isCropping) onDismiss() },
        icon = Icons.Default.CropFree,
        title = Strings.cropIcon,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Strings.cropDragHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val modes = IconCropMode.entries
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = cropMode == mode,
                            onClick = { cropMode = mode },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                        ) {
                            Text(
                                text = when (mode) {
                                    IconCropMode.SQUARE -> Strings.cropRatioSquare
                                    IconCropMode.FREE -> Strings.cropRatioFree
                                    IconCropMode.CIRCLE -> Strings.cropRatioCircle
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onSizeChanged { previewSize = it },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = Strings.loadingImage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        originalBitmap != null && cropRect != Rect.Zero -> {
                            val bitmap = originalBitmap!!
                            val drawnWidth = bitmap.width * scale
                            val drawnHeight = bitmap.height * scale

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(cropRect, minScale, maxScale) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(minScale, maxScale)

                                            // Keep the image covering the crop
                                            // window; if it is smaller than the
                                            // window on an axis, center that axis.
                                            val halfW = bitmap.width * scale / 2f
                                            val halfH = bitmap.height * scale / 2f
                                            val centerX = cropRect.center.x
                                            val centerY = cropRect.center.y

                                            val minOffsetX = cropRect.right - drawnWidth - centerX + halfW
                                            val maxOffsetX = cropRect.left - centerX + halfW
                                            offsetX = if (minOffsetX > maxOffsetX) {
                                                0f
                                            } else {
                                                (offsetX + pan.x).coerceIn(minOffsetX, maxOffsetX)
                                            }

                                            val minOffsetY = cropRect.bottom - drawnHeight - centerY + halfH
                                            val maxOffsetY = cropRect.top - centerY + halfH
                                            offsetY = if (minOffsetY > maxOffsetY) {
                                                0f
                                            } else {
                                                (offsetY + pan.y).coerceIn(minOffsetY, maxOffsetY)
                                            }
                                        }
                                    }
                            ) {
                                val density = LocalDensity.current
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = Strings.originalImage,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset {
                                            IntOffset(
                                                (offsetX - drawnWidth / 2f).roundToInt(),
                                                (offsetY - drawnHeight / 2f).roundToInt()
                                            )
                                        }
                                        .size(
                                            width = with(density) { drawnWidth.toDp() },
                                            height = with(density) { drawnHeight.toDp() }
                                        )
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val dim = Color.Black.copy(alpha = 0.55f)
                                    when (cropMode) {
                                        IconCropMode.CIRCLE -> {
                                            val circle = Path().apply { addOval(cropRect) }
                                            clipPath(circle, ClipOp.Difference) {
                                                drawRect(dim)
                                            }
                                            drawOval(
                                                color = Color.White,
                                                topLeft = cropRect.topLeft,
                                                size = cropRect.size,
                                                style = Stroke(width = 2.dp.toPx())
                                            )
                                        }
                                        IconCropMode.SQUARE -> {
                                            clipRect(
                                                cropRect.left, cropRect.top,
                                                cropRect.right, cropRect.bottom,
                                                ClipOp.Difference
                                            ) {
                                                drawRect(dim)
                                            }
                                            drawRect(
                                                color = Color.White,
                                                topLeft = cropRect.topLeft,
                                                size = cropRect.size,
                                                style = Stroke(width = 2.dp.toPx())
                                            )
                                        }
                                        IconCropMode.FREE -> {
                                            drawRect(dim)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (originalBitmap != null && cropRect != Rect.Zero) {
                    val outputLabel = remember(cropRect, scale) {
                        val srcWidth = cropRect.width / scale
                        val srcHeight = cropRect.height / scale
                        if (srcWidth >= srcHeight) {
                            val outH = max(1, (srcHeight / srcWidth * CROP_OUTPUT_SIZE).roundToInt())
                            "${CROP_OUTPUT_SIZE} × $outH px"
                        } else {
                            val outW = max(1, (srcWidth / srcHeight * CROP_OUTPUT_SIZE).roundToInt())
                            "$outW × ${CROP_OUTPUT_SIZE} px"
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = Strings.cropOriginalSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${originalBitmap!!.width} × ${originalBitmap!!.height} px",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Strings.cropOutputSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = outputLabel,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (isCropping) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val bitmap = originalBitmap ?: return@TextButton
                    if (cropRect == Rect.Zero) return@TextButton
                    isCropping = true
                    scope.launch {
                        val cropped = cropBitmap(
                            bitmap = bitmap,
                            cropRect = cropRect,
                            scale = scale,
                            offsetX = offsetX,
                            offsetY = offsetY
                        )
                        val item = cropped?.let {
                            IconLibraryStorage.saveFromBitmap(context, it, Strings.cropIcon)
                        }
                        cropped?.recycle()
                        isCropping = false
                        if (item != null) {
                            onCropComplete(item.path)
                        } else {
                            errorMessage = Strings.saveFailed
                        }
                    }
                },
                enabled = originalBitmap != null && !isCropping
            ) {
                Text(Strings.confirmCrop)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCropping) {
                Text(Strings.btnCancel)
            }
        }
    )
}

/**
 * Maps the visible crop window from preview coordinates back to bitmap
 * coordinates and extracts exactly the region the user saw. The image is
 * drawn centered on the preview box, shifted by (offsetX, offsetY) and scaled
 * by `scale`, so bitmap (0,0) sits at preview center + offset - half drawn
 * size. The result is scaled so its longest side becomes CROP_OUTPUT_SIZE.
 */
private suspend fun cropBitmap(
    bitmap: Bitmap,
    cropRect: Rect,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val drawnWidth = bitmap.width * scale
        val drawnHeight = bitmap.height * scale
        val imageLeft = cropRect.center.x + offsetX - drawnWidth / 2f
        val imageTop = cropRect.center.y + offsetY - drawnHeight / 2f

        val srcLeft = ((cropRect.left - imageLeft) / scale).roundToInt().coerceIn(0, bitmap.width - 1)
        val srcTop = ((cropRect.top - imageTop) / scale).roundToInt().coerceIn(0, bitmap.height - 1)
        val srcRight = ((cropRect.right - imageLeft) / scale).roundToInt().coerceIn(srcLeft + 1, bitmap.width)
        val srcBottom = ((cropRect.bottom - imageTop) / scale).roundToInt().coerceIn(srcTop + 1, bitmap.height)

        val srcWidth = srcRight - srcLeft
        val srcHeight = srcBottom - srcTop
        if (srcWidth <= 0 || srcHeight <= 0) return@withContext null

        val cropped = Bitmap.createBitmap(bitmap, srcLeft, srcTop, srcWidth, srcHeight)
        val output = if (srcWidth >= srcHeight) {
            val outHeight = max(1, (srcHeight.toFloat() / srcWidth * CROP_OUTPUT_SIZE).roundToInt())
            Bitmap.createScaledBitmap(cropped, CROP_OUTPUT_SIZE, outHeight, true)
        } else {
            val outWidth = max(1, (srcWidth.toFloat() / srcHeight * CROP_OUTPUT_SIZE).roundToInt())
            Bitmap.createScaledBitmap(cropped, outWidth, CROP_OUTPUT_SIZE, true)
        }
        if (output !== cropped) {
            cropped.recycle()
        }
        AppLogger.i("IconCropDialog", "Icon cropped: ${output.width}x${output.height}")
        output
    } catch (e: Exception) {
        AppLogger.e("IconCropDialog", "Crop failed", e)
        null
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
