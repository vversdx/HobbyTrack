package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.viewmodels.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.R

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Логотип приложения",
            modifier = Modifier
                .size(140.dp)
                .padding(top = 10.dp, bottom = 16.dp)
        )

        Text(
            text = "Вход",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5883D4),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Заполните все поля"
                    return@Button
                }
                isLoading = true
                authViewModel.login(email, password) { success, message ->
                    isLoading = false
                    if (success) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        errorMessage = message ?: "Ошибка входа"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator()
            else Text("Продолжить")
        }

        TextButton(
            onClick = { navController.navigate(Screen.SignUp.route) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text ="Еще не зарегистрированы? Регистрация",
                color = Color.Gray
            )
        }
    }
}