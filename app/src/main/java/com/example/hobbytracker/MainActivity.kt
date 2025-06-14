package com.example.hobbytracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.hobbytracker.navigation.NavGraph
import com.example.hobbytracker.ui.components.AppSettings
import com.example.hobbytracker.viewmodels.SettingsViewModel
import com.example.hobbytracker.viewmodels.SettingsViewModelFactory
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация зависимостей
        val appSettings = AppSettings(this)
        val viewModelFactory = SettingsViewModelFactory(appSettings)

        // Получение ViewModel с фабрикой
        val viewModel: SettingsViewModel by viewModels { viewModelFactory }

        setContent {
            val darkTheme = viewModel.darkTheme.value

            HobbyTrackerTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

