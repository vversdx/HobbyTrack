package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hobbytracker.R
import com.example.hobbytracker.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme.isDarkTheme
import com.example.hobbytracker.util.ColorUtils

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by remember { derivedStateOf { authViewModel.auth.currentUser } }

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(if (currentUser != null) "main" else "login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Text(
            text = "Пожалуйста, подождите",
            color = Color.Gray,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 45.dp)
        )

        Text(
            text = "HobbyTrack",
            style = MaterialTheme.typography.displayMedium,
            fontSize = 50.sp,
            modifier = Modifier.align(Alignment.Center),
            color = if (isDarkTheme) ColorUtils.GrayPrimaryDark else Color.Black
        )

        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Логотип",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
                .offset(y = (-150).dp)
        )
    }
}