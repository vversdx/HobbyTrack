package com.example.hobbytracker.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbytracker.ui.components.AppSettings
import kotlinx.coroutines.launch

class SettingsViewModel(private val appSettings: AppSettings) : ViewModel() {
    private val _darkTheme = mutableStateOf(false)
    val darkTheme: State<Boolean> = _darkTheme

    init {
        viewModelScope.launch {
            appSettings.darkThemeFlow.collect { isDark ->
                _darkTheme.value = isDark
            }
        }
    }

    fun toggleTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        viewModelScope.launch {
            appSettings.saveDarkTheme(enabled)
        }
    }
}