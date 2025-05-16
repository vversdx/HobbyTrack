package com.example.hobbytracker.util

import androidx.compose.ui.graphics.Color

object ColorUtils {
    private val avatarColors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC6),
        Color(0xFF018786),
        Color(0xFFBB86FC),
        Color(0xFF3700B3)
    )

    val BluePrimary = Color(0xFF4B81D3)
    val GraySecondary = Color(0xFF9DA3AB)
    val MainBlue = Color(0xFFC7E1FF)

    fun getRandomColorForInitials(initials: String): Color {
        return avatarColors[initials.sumOf { it.code } % avatarColors.size]
    }
}