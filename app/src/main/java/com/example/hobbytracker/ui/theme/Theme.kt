package com.example.hobbytracker.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hobbytracker.R

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4B81D3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColorScheme = lightColorScheme(
    primary = Color(0xFF4B81D3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF061045),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

val Ribeye = FontFamily(
    Font(R.font.ribeye_regular, weight = FontWeight.Normal)
)

private val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = Ribeye,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Ribeye,
        fontSize = 16.sp
    )
)

object HobbyTrackerTheme {
    var isDarkTheme by mutableStateOf(false)
}

@Composable
fun HobbyTrackerTheme(
    darkTheme: Boolean = HobbyTrackerTheme.isDarkTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    HobbyTrackerTheme.isDarkTheme = darkTheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
