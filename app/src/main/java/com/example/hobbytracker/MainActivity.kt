package com.example.hobbytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hobbytracker.navigation.NavGraph
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.navigation.rememberAppNavigationState
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HobbyTrackerTheme {
                val navController = rememberNavController()
                val navigationState = rememberAppNavigationState(navController)

                NavGraph(
                    navigationState = navigationState,
                    startDestination = Screen.Splash.route
                )
            }
        }
    }
}

//@Composable
//fun MyApp() {
//    val navigationState = rememberAppNavigationState()
//
//    NavGraph(
//        navigationState = navigationState,
//        startDestination = Screen.Splash.route
//    )
//}