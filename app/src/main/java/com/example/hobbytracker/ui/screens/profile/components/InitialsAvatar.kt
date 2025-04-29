package com.example.hobbytracker.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.hobbytracker.util.ColorUtils.getRandomColorForInitials

@Composable
fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,
    backgroundColor: Color = getRandomColorForInitials(initials)
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(2).uppercase(),
            color = Color.White,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}