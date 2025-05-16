package com.example.hobbytracker.ui.screens.profile.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun AvatarWithEdit(
    avatarBase64: String?,
    initials: String,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(avatarBase64) {
        avatarBase64?.let {
            try {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier.size(120.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (bitmap == null) MaterialTheme.colorScheme.surfaceVariant
            else Color.Transparent,
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.Center)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Аватар",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = initials.take(2).uppercase(),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }

        IconButton(
            onClick = onEditClicked,
            modifier = Modifier
                .size(36.dp)
                .offset((-8).dp, (-8).dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Изменить аватар",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}