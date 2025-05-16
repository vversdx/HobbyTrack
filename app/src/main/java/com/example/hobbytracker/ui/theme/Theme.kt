package com.example.hobbytracker.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hobbytracker.R
import com.example.hobbytracker.util.ColorUtils

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DarkColorScheme = lightColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
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

@Composable
fun HobbyTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun OutlinedText(
    text: String,
    textColor: Color = Color.White,
    outlineColor: Color = Color(0xFF4B81D3),
    outlineWidth: Dp = 1.5.dp,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Box(modifier = modifier) {
        val offsets = listOf(
            Offset(-outlineWidth.value, 0f),
            Offset(outlineWidth.value, 0f),
            Offset(0f, -outlineWidth.value),
            Offset(0f, outlineWidth.value)
        )

        offsets.forEach { offset ->
            Text(
                text = text,
                color = outlineColor,
                style = textStyle,
                modifier = Modifier.offset(
                    x = offset.x.dp,
                    y = offset.y.dp
                )
            )
        }

        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}