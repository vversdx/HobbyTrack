package com.example.hobbytracker.ui.screens.profile.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AvatarEditor(
    imageUri: Uri?,
    initials: String,
    size: Dp = 120.dp,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onEditClicked() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Аватар профиля",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            InitialsAvatar(
                initials = initials,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}