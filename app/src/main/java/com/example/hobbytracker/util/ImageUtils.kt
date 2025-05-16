package com.example.hobbytracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun cropBitmapToRect(bitmap: Bitmap, rect: RectF): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        return Bitmap.createBitmap(
            bitmap,
            (rect.left * width).toInt(),
            (rect.top * height).toInt(),
            ((rect.right - rect.left) * width).toInt(),
            ((rect.bottom - rect.top) * height).toInt()
        )
    }

    // Сжатие изображения с сохранением пропорций
    fun compressAvatar(bitmap: Bitmap, maxSize: Int = 500): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        val newWidth = if (width > height) maxSize else (maxSize * ratio).toInt()
        val newHeight = if (height > width) maxSize else (maxSize / ratio).toInt()

        return bitmap.scale(newWidth, newHeight)
    }

    // Конвертация в Base64
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 70): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    // Обратная конвертация
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }


    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error loading image", e)
            null
        }
    }

    fun copyImageToCache(context: Context, uri: Uri): Uri {
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(file)
    }
}
