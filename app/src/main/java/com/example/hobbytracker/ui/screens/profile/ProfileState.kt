package com.example.hobbytracker.ui.screens.profile

import android.graphics.Bitmap

data class ProfileState(
    val profileImage: Bitmap? = null,
    val photoUrl: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String? = null,
    val email: String = "",
    val phone: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)