package com.example.hobbytracker.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hobbytracker.ui.screens.LoginScreen
import com.example.hobbytracker.ui.screens.MainScreen
import com.example.hobbytracker.ui.screens.SettingsScreen
import com.example.hobbytracker.ui.screens.SignUpScreen
import com.example.hobbytracker.ui.screens.SplashScreen
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

    }
}