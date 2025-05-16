package com.example.hobbytracker.navigation

import ImageCropperScreen
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hobbytracker.ui.screens.LoginScreen
import com.example.hobbytracker.ui.screens.MainScreen
import com.example.hobbytracker.ui.screens.SignUpScreen
import com.example.hobbytracker.ui.screens.SplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.hobbytracker.ui.screens.profile.ProfileScreen
import java.net.URLDecoder

class AppNavigationState(
    val navController: NavHostController
) {
    private var _selectedImageUri by mutableStateOf<Uri?>(null)

    val selectedImageUri: Uri?
        get() = _selectedImageUri.also {
            Log.d("NAV_DEBUG", "Current URI: $it")
        }

    private var _croppedBitmap by mutableStateOf<Bitmap?>(null)
    val croppedBitmap: Bitmap? get() = _croppedBitmap


    fun setSelectedImageUri(uri: Uri) {
        _selectedImageUri = uri
        Log.d("NAV_DEBUG", "URI set: $uri")
    }

    fun updateCroppedBitmap(bitmap: Bitmap) {
        _croppedBitmap = bitmap
    }

    fun clearImageData() {
        _selectedImageUri = null
        _croppedBitmap = null
    }
}

@Composable
fun rememberAppNavigationState(
    navController: NavHostController = rememberNavController()
): AppNavigationState {
    return remember(navController) {
        AppNavigationState(navController)
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    navigationState: AppNavigationState = rememberAppNavigationState(),
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

        composable("profile") { ProfileScreen(navController) }

    }
}