package com.example.hobbytracker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hobbytracker.ui.components.AppSettings

class SettingsViewModelFactory(
    private val appSettings: AppSettings
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(appSettings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}