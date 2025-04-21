package com.example.hobbytracker.data

data class HobbyCategory(
    val id: String,
    val name: String,
    val hobbyCount: Int = 0,
    val iconRes: Int
)