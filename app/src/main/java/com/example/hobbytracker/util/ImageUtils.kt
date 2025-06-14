@file:Suppress("DEPRECATION")

package com.example.hobbytracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import androidx.core.net.toUri
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection

object ImageUtils {
    private fun getRotationDegrees(context: Context, uri: Uri): Int {
        return when (context.contentResolver.getType(uri)) {
            "image/jpeg", "image/jpg" -> {
                try {
                    val exif = ExifInterface(context.contentResolver.openInputStream(uri)!!)
                    when (exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                } catch (e: Exception) {
                    0
                }
            }
            else -> 0
        }
    }

    fun loadImageFromUrl(url: String): Bitmap? {
        return try {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.doInput = true
            urlConnection.connect()
            val input = urlConnection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }

    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "ProfileImage",
                null
            )
            path.toUri()
        } catch (e: Exception) {
            null
        }
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, userId: String): Boolean {
        return try {
            val file = File(context.cacheDir, "profile_$userId.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadBitmapFromCache(context: Context, userId: String): Bitmap? {
        return try {
            val file = File(context.cacheDir, "profile_$userId.jpg")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun clearUserCache(context: Context, userId: String) {
        try {
            val file = File(context.cacheDir, "profile_$userId.jpg")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error clearing cache", e)
        }
    }
}