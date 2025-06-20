package com.example.hobbytracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hobbytracker.ui.screens.ActivityScreen
import com.example.hobbytracker.ui.screens.ArtHobbyScreen
import com.example.hobbytracker.ui.screens.GamesHobbyScreen
import com.example.hobbytracker.ui.screens.LoginScreen
import com.example.hobbytracker.ui.screens.MainScreen
import com.example.hobbytracker.ui.screens.MusicHobbyScreen
import com.example.hobbytracker.ui.screens.OtherHobbyScreen
import com.example.hobbytracker.ui.screens.ReadingHobbyScreen
import com.example.hobbytracker.ui.screens.SettingsScreen
import com.example.hobbytracker.ui.screens.SignUpScreen
import com.example.hobbytracker.ui.screens.SplashScreen
import com.example.hobbytracker.ui.screens.SportHobbyScreen
import com.example.hobbytracker.ui.screens.profile.ProfileScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Main : Screen("main")
    object Profile : Screen("profile")
    object HobbyCategory : Screen("hobby_category/{categoryId}") {
        fun createRoute(categoryId: String) = "hobby_category/$categoryId"
    }
    object Settings : Screen("settings")
    object Activity : Screen("activity")
    object MusicHobby : Screen("music")
    object SportHobby : Screen("sport")
    object ArtHobby : Screen("art")
    object ReadingHobby : Screen("reading")
    object GamesHobby : Screen("games")
    object OtherHobby : Screen("other")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.Activity.route) {
            ActivityScreen(navController = navController)
        }

        composable(Screen.MusicHobby.route) {
            MusicHobbyScreen(navController = navController)
        }

        composable(Screen.SportHobby.route) {
            SportHobbyScreen(navController = navController)
        }

        composable(Screen.ArtHobby.route) {
            ArtHobbyScreen(navController = navController)
        }

        composable(Screen.ReadingHobby.route) {
            ReadingHobbyScreen(navController = navController)
        }

        composable(Screen.GamesHobby.route) {
            GamesHobbyScreen(navController = navController)
        }

        composable(Screen.OtherHobby.route) {
            OtherHobbyScreen(navController = navController)
        }

    }
}