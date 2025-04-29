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

    fun getRandomColorForInitials(initials: String): Color {
        return avatarColors[initials.sumOf { it.code } % avatarColors.size]
    }
}