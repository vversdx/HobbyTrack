package com.example.hobbytracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.net.Uri
import java.io.InputStream

object ImageUtils {
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

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
}