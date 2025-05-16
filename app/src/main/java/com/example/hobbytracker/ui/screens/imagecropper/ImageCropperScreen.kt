import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import java.net.URLDecoder
import kotlin.math.max
import kotlin.math.min
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropperScreen(
    navController: NavController,
    imageUri: String,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }

    // Загрузка изображения с оптимизацией
    LaunchedEffect(imageUri) {
        try {
            val decodedUri = URLDecoder.decode(imageUri, "UTF-8")
            val uri = decodedUri.toUri()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, options)
                stream.close()

                val sampleSize = calculateInSampleSize(options, 1024, 1024)

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                }

                context.contentResolver.openInputStream(uri)?.use { newStream ->
                    bitmap = BitmapFactory.decodeStream(newStream, null, decodeOptions)?.also {
                        imageSize = Size(it.width.toFloat(), it.height.toFloat())
                    }
                    scale = 1f
                    offset = Offset.Zero
                }
            }
        } catch (e: Exception) {
            navController.popBackStack()
        }
    }

    // Размеры области обрезки (80% от меньшей стороны)
    val cropSize = min(canvasSize.width, canvasSize.height) * 0.8f
    val cropRect = remember(canvasSize) {
        Rect(
            left = (canvasSize.width - cropSize) / 2,
            top = (canvasSize.height - cropSize) / 2,
            right = (canvasSize.width + cropSize) / 2,
            bottom = (canvasSize.height + cropSize) / 2
        )
    }

    // Рассчитываем размеры отображения с сохранением пропорций
    val (displayWidth, displayHeight) = remember(imageSize, canvasSize) {
        if (imageSize.width <= 0 || imageSize.height <= 0) return@remember 0f to 0f

        val imageRatio = imageSize.width / imageSize.height
        val canvasRatio = canvasSize.width / canvasSize.height

        if (imageRatio > canvasRatio) {
            // Широкое изображение (альбомная ориентация)
            canvasSize.width to (canvasSize.width / imageRatio)
        } else {
            // Высокое изображение (портретная ориентация)
            (canvasSize.height * imageRatio) to canvasSize.height
        }
    }

    // Ограничители для перемещения (точные)
    fun updateOffset(newOffset: Offset) {
        if (displayWidth <= 0 || displayHeight <= 0) {
            Log.e("CROP_DEBUG", "Некорректные размеры отображения")
            return
        }

        val maxOffsetX = max(0f, (displayWidth * scale - cropSize) / 2)
        val maxOffsetY = max(0f, (displayHeight * scale - cropSize) / 2)

        Log.d("CROP_DEBUG", "Новое смещение до ограничения: $newOffset")
        Log.d("CROP_DEBUG", "Максимальные смещения: X=$maxOffsetX, Y=$maxOffsetY")

        offset = Offset(
            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )

        Log.d("CROP_DEBUG", "Установлено смещение: $offset")
    }

    // Функция обработки нажатия на галочку (исправленная)
    fun handleCrop() {
        bitmap?.let { originalBitmap ->
            try {
                Log.d("CROP_DEBUG", "=== Начало обработки обрезки ===")
                Log.d("CROP_DEBUG", "Размер оригинального изображения: ${originalBitmap.width}x${originalBitmap.height}")
                Log.d("CROP_DEBUG", "Размер canvas: $canvasSize")
                Log.d("CROP_DEBUG", "Масштаб: $scale, Смещение: $offset")

                // 1. Рассчитываем соотношение размеров изображения и области отображения
                val imageRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                val displayWidth: Float
                val displayHeight: Float

                if (imageRatio > canvasSize.width / canvasSize.height) {
                    // Широкое изображение (ширина ограничивающая)
                    displayWidth = canvasSize.width
                    displayHeight = canvasSize.width / imageRatio
                } else {
                    // Высокое изображение (высота ограничивающая)
                    displayHeight = canvasSize.height
                    displayWidth = canvasSize.height * imageRatio
                }

                Log.d("CROP_DEBUG", "Размер отображения: ${displayWidth}x${displayHeight}")

                // 2. Коэффициенты преобразования из экранных координат в координаты изображения
                val scaleX = originalBitmap.width.toFloat() / displayWidth
                val scaleY = originalBitmap.height.toFloat() / displayHeight

                Log.d("CROP_DEBUG", "Коэффициенты масштабирования: scaleX=$scaleX, scaleY=$scaleY")

                // 3. Центр обрезки в координатах изображения (с учетом масштаба и смещения)
                val centerX = (canvasSize.width / 2 - offset.x) * scaleX / scale
                val centerY = (canvasSize.height / 2 - offset.y) * scaleY / scale

                Log.d("CROP_DEBUG", "Центр обрезки в координатах изображения: ($centerX, $centerY)")

                // 4. Размер области обрезки в пикселях изображения
                val cropSizePx = cropSize * min(scaleX, scaleY) / scale

                Log.d("CROP_DEBUG", "Размер обрезки в пикселях: $cropSizePx")

                // 5. Рассчитываем границы обрезки
                val left = (centerX - cropSizePx / 2).coerceAtLeast(0f)
                val top = (centerY - cropSizePx / 2).coerceAtLeast(0f)
                val right = (centerX + cropSizePx / 2).coerceAtMost(originalBitmap.width.toFloat())
                val bottom = (centerY + cropSizePx / 2).coerceAtMost(originalBitmap.height.toFloat())

                Log.d("CROP_DEBUG", "Координаты обрезки: L=$left, T=$top, R=$right, B=$bottom")

                val width = (right - left).toInt().coerceAtLeast(1)
                val height = (bottom - top).toInt().coerceAtLeast(1)

                Log.d("CROP_DEBUG", "Размер обрезанного изображения: ${width}x${height}")

                if (width > 0 && height > 0) {
                    val cropped = Bitmap.createBitmap(
                        originalBitmap,
                        left.toInt(),
                        top.toInt(),
                        width,
                        height
                    )

                    Log.d("CROP_DEBUG", "Успешно создано обрезанное изображение")

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("cropped_bitmap", cropped)
                    navController.popBackStack()
                }
            } catch (e: Exception) {
                Log.e("CROP_DEBUG", "Ошибка при обрезке: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Обрезка фото") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { handleCrop() },
                        enabled = bitmap != null
                    ) {
                        Icon(Icons.Default.Check, "Готово")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                        Log.d("CROP_DEBUG", "Изменение масштаба: $scale -> $newScale")
                        scale = newScale
                        val newOffset = offset + pan
                        Log.d("CROP_DEBUG", "Запрошено изменение смещения: $offset -> $newOffset")
                        updateOffset(newOffset)
                    }
                }
                .onSizeChanged { canvasSize = it.toSize() }
        ) {
            bitmap?.let { originalBitmap ->
                Box(
                    modifier = Modifier
                        .width(displayWidth.dp)
                        .height(displayHeight.dp)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                ) {
                    Image(
                        bitmap = originalBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Рамка обрезки
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cropRect.left, cropRect.top),
                        size = Size(cropSize, cropSize),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Угловые маркеры
                    val markerSize = 16.dp.toPx()
                    listOf(
                        Offset(cropRect.left, cropRect.top),
                        Offset(cropRect.right, cropRect.top),
                        Offset(cropRect.left, cropRect.bottom),
                        Offset(cropRect.right, cropRect.bottom)
                    ).forEach { corner ->
                        drawRect(
                            color = Color.White,
                            topLeft = corner - Offset(markerSize/2, markerSize/2),
                            size = Size(markerSize, markerSize),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}
