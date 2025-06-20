package com.example.hobbytracker.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme.isDarkTheme

object ColorUtils {
    private val avatarColors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC6),
        Color(0xFF018786),
        Color(0xFFBB86FC),
        Color(0xFF3700B3)
    )

    val BluePrimaryLight = Color(0xFF4B81D3)
    val GrayPrimaryDark = Color(0xFFCBBCBC)

    val GraySecondary = Color(0xFF9DA3AB)

    val MainBlueLight = Color(0xFFC7E1FF)
    val MainBlueDark = Color(0xFF061045)

    val HobbyCategoryLight = Color(0xFFC7E1FF)
    val HobbyCategoryDark = Color(0xFFDDE6F4)

    val TopBarLight = Color(0xFFD9E3FB)
    val TopBarDark = Color(0xFFDDE6F4)

    val translucentLight = BluePrimaryLight.copy(alpha = 0.8f)
    val transientDark = Color(0xFF28355D).copy(alpha = 0.8f)

    val GrayProfile = Color(0xFFEFF6FF)

    val TextFieldColor = Color(0xFFD9D9D9)

    @Composable
    fun MainBlue(): Color {
        return if (isDarkTheme) MainBlueDark else MainBlueLight
    }

    @Composable
    fun TopBarColor(): Color {
        return if (isDarkTheme) TopBarDark else TopBarLight
    }

    @Composable
    fun PrimaryColor(): Color {
        return if (isDarkTheme) GrayPrimaryDark else BluePrimaryLight
    }

    @Composable
    fun translucentColor(): Color {
            return if (isDarkTheme) transientDark else translucentLight
    }

    @Composable
    fun HobbyCategoryColor(): Color {
        return if (isDarkTheme) HobbyCategoryDark else HobbyCategoryLight
    }

    @Composable
    fun CategoryName(): Color {
        return if (isDarkTheme) MainBlueDark else Color.White
    }

    fun getRandomColorForInitials(initials: String): Color {
        return avatarColors[initials.sumOf { it.code } % avatarColors.size]
    }

    fun getInitials(firstName: String?, lastName: String?): String {
        val first = firstName?.take(1)?.uppercase() ?: ""
        val last = lastName?.take(1)?.uppercase() ?: ""
        return (first + last).take(2)
    }
}