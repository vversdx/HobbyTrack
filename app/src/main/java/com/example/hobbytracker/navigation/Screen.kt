package com.example.hobbytracker.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Main : Screen("main")
    object Profile : Screen("profile")
    object AddHobby : Screen("add_hobby")
    object HobbyCategory : Screen("hobby_category/{categoryId}") {
        fun createRoute(categoryId: String) = "hobby_category/$categoryId"
    }
}