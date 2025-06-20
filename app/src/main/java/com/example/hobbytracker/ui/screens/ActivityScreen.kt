package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbytracker.ui.components.NewTopAppBar
import com.example.hobbytracker.util.ColorUtils
import com.example.hobbytracker.viewmodels.ArtCraftViewModel
import com.example.hobbytracker.viewmodels.GamePlayViewModel
import com.example.hobbytracker.viewmodels.MusicHobbyViewModel
import com.example.hobbytracker.viewmodels.ReadingBookViewModel
import com.example.hobbytracker.viewmodels.SportActivityViewModel


@Composable
fun ActivityScreen(navController: NavController) {
    val sportVM: SportActivityViewModel = viewModel()
    val sportsTime by sportVM.sportActivityTotalDuration.collectAsState()
    val musicVM: MusicHobbyViewModel = viewModel()
    val musicTime by musicVM.musicHobbyTotalTime.collectAsState()
    val gamesVM: GamePlayViewModel = viewModel()
    val gamesTime by gamesVM.gamePlayTotalDuration.collectAsState()
    val readingVM: ReadingBookViewModel = viewModel()
    val readingTime by readingVM.readingBookTotalDuration.collectAsState()
    val artVM: ArtCraftViewModel = viewModel()
    val artTime by artVM.artCraftTotalDuration.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 60.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "Моя активность",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorUtils.HobbyCategoryColor(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                HobbyLegendItem(
                    color = Color.Red,
                    name = "Спорт",
                    time = sportsTime
                )
                Spacer(modifier = Modifier.height(8.dp))
                HobbyLegendItem(
                    color = Color.Yellow,
                    name = "Искусство",
                    time = artTime
                )
                Spacer(modifier = Modifier.height(8.dp))
                HobbyLegendItem(
                    color = Color.Magenta,
                    name = "Музыка",
                    time = musicTime
                )
                Spacer(modifier = Modifier.height(8.dp))
                HobbyLegendItem(
                    color = Color.Blue,
                    name = "Чтение",
                    time = readingTime
                )
                Spacer(modifier = Modifier.height(8.dp))
                HobbyLegendItem(
                    color = Color(0xFF8A2BE2),
                    name = "Игры",
                    time = gamesTime
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            VerticalBarChart(
                sportsTime = sportsTime,
                artTime = artTime,
                musicTime = musicTime,
                readingTime = readingTime,
                gamesTime = gamesTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            )
        }
        NewTopAppBar(navController = navController)
    }
}
@Composable
fun HobbyLegendItem(color: Color, name: String, time: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .size(16.dp)
        ) {
            drawCircle(color = color)
        }
        Text(
            text = "$name: $time мин",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun VerticalBarChart(
    sportsTime: Int,
    artTime: Int,
    musicTime: Int,
    readingTime: Int,
    gamesTime: Int,
    modifier: Modifier = Modifier
) {
    val maxTime = maxOf(sportsTime, artTime, musicTime, readingTime, gamesTime).coerceAtLeast(1)
    val categories = listOf("Спорт", "Искусство", "Музыка", "Чтение", "Игры")
    val times = listOf(sportsTime, artTime, musicTime, readingTime, gamesTime)
    val colors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Magenta,
        Color.Blue,
        Color(0xFF8A2BE2)
    )

    Canvas(modifier = modifier) {
        val totalBars = categories.size
        val totalSpacing = size.width * 0.1f
        val barWidth = (size.width - totalSpacing) / totalBars
        val spacing = totalSpacing / (totalBars + 1)
        val maxBarHeight = size.height * 0.7f
        val zeroLine = size.height * 0.8f
        val cornerRadius = 8.dp.toPx()

        fun createTopRoundedRectPath(left: Float, top: Float, width: Float, height: Float): Path {
            return Path().apply {
                moveTo(left + cornerRadius, top)
                lineTo(left + width - cornerRadius, top)
                arcTo(
                    Rect(left + width - 2 * cornerRadius, top, left + width, top + 2 * cornerRadius),
                    270f,
                    90f,
                    false
                )
                lineTo(left + width, top + height)
                lineTo(left, top + height)
                lineTo(left, top + cornerRadius)
                arcTo(
                    Rect(left, top, left + 2 * cornerRadius, top + 2 * cornerRadius),
                    180f,
                    90f,
                    false
                )
                close()
            }
        }

        categories.forEachIndexed { index, _ ->
            val barHeight = (times[index].toFloat() / maxTime) * maxBarHeight
            val left = spacing + index * (barWidth + spacing)
            val top = zeroLine - barHeight
            drawRect(
                color = Color.Gray.copy(alpha = 0.3f),
                topLeft = Offset(left + 4f, top + 4f),
                size = Size(barWidth, barHeight)
            )
            val path = createTopRoundedRectPath(left, top, barWidth, barHeight)
            drawPath(
                path = path,
                color = colors[index]
            )
            drawContext.canvas.nativeCanvas.apply {
                val valueText = "${times[index]} мин"
                val textPaint = android.graphics.Paint().apply {
                    textSize = 28f
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val textY = if (barHeight > 40f) top - 15f else zeroLine + 60f
                drawText(
                    valueText,
                    left + barWidth / 2,
                    textY,
                    textPaint
                )
            }
            drawContext.canvas.nativeCanvas.apply {
                val categoryPaint = android.graphics.Paint().apply {
                    textSize = 24f
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    categories[index],
                    left + barWidth / 2,
                    zeroLine + 45f,
                    categoryPaint
                )
            }
        }
        drawLine(
            color = Color.Black.copy(alpha = 0.5f),
            start = Offset(0f, zeroLine),
            end = Offset(size.width, zeroLine),
            strokeWidth = 2f
        )
        val gridLines = 4
        repeat(gridLines) { i ->
            val y = zeroLine - (maxBarHeight * (i + 1) / gridLines)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
    }
}