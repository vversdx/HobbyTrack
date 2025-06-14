package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hobbytracker.R
import com.example.hobbytracker.viewmodels.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.util.ColorUtils
import com.example.hobbytracker.ui.theme.Ribeye
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .background(Color.Transparent)
            ) {

                // Кнопка назад
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = ColorUtils.PrimaryColor()
                    )
                }

                // Центральное содержимое
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Логотип",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Регистрация",
                        color = ColorUtils.PrimaryColor(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя", color = ColorUtils.GraySecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorUtils.TextFieldColor),
                textStyle = LocalTextStyle.current.copy(color = Color.Black)
            )

            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия", color = ColorUtils.GraySecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorUtils.TextFieldColor),
                textStyle = LocalTextStyle.current.copy(color = Color.Black)
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = ColorUtils.GraySecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorUtils.TextFieldColor),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль", color = ColorUtils.GraySecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorUtils.TextFieldColor),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердите пароль", color = ColorUtils.GraySecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorUtils.TextFieldColor),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (password != confirmPassword) {
                            errorMessage = "Пароли не совпадают"
                            return@clickable
                        }
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Заполните все поля"
                            return@clickable
                        }
                        if (firstName.isBlank() || lastName.isBlank()) {
                            errorMessage = "Введите имя и фамилию"
                            return@clickable
                        }

                        isLoading = true
                        coroutineScope.launch {
                            val result = authViewModel.signUp(
                                email = email,
                                password = password,
                                firstName = firstName,
                                lastName = lastName
                            )
                            isLoading = false

                            if (result.isSuccess) {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Ошибка регистрации"
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF5883D4)
                    )
                } else {
                    Text(
                        text = "Зарегистрироваться",
                        color = ColorUtils.PrimaryColor(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
