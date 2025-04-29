package com.example.hobbytracker.ui.screens.imagecropper

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hobbytracker.util.ImageUtils.loadBitmapFromUri
import com.example.hobbytracker.util.ImageUtils.cropBitmapToRect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropperScreen(
    imageUri: String,
    onComplete: (Bitmap) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cropRect by remember { mutableStateOf(RectF(0.25f, 0.25f, 0.75f, 0.75f)) }
    val bitmap = remember(imageUri) {
        loadBitmapFromUri(context, Uri.parse(imageUri))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Обрезка фото") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            bitmap?.let {
                                onComplete(cropBitmapToRect(it, cropRect))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Применить")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            bitmap?.asImageBitmap()?.let { img ->
                Image(
                    bitmap = img,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                cropRect = RectF(
                                    (cropRect.left + dragAmount.x / size.width).coerceIn(0f, 0.9f),
                                    (cropRect.top + dragAmount.y / size.height).coerceIn(0f, 0.9f),
                                    (cropRect.right + dragAmount.x / size.width).coerceIn(0.1f, 1f),
                                    (cropRect.bottom + dragAmount.y / size.height).coerceIn(0.1f, 1f)
                                )
                            }
                        }
                ) {
                    drawRect(
                        color = Color.Blue.copy(alpha = 0.3f),
                        topLeft = Offset(cropRect.left * size.width, cropRect.top * size.height),
                        size = Size(
                            (cropRect.right - cropRect.left) * size.width,
                            (cropRect.bottom - cropRect.top) * size.height
                        ),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}