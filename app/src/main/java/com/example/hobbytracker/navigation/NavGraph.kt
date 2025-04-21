package com.example.hobbytracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hobbytracker.ui.screens.LoginScreen
import com.example.hobbytracker.ui.screens.MainScreen
import com.example.hobbytracker.ui.screens.SignUpScreen
import com.example.hobbytracker.ui.screens.SplashScreen
import com.example.hobbytracker.viewmodels.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
            SplashScreen(
                navController = navController,
                authViewModel = viewModel()
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = viewModel()
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                navController = navController,
                authViewModel = viewModel()
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }
    }
}