package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.example.hobbytracker.viewmodels.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.R
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.util.ColorUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Логотип приложения",
                    modifier = Modifier
                        .size(140.dp)
                        .padding(bottom = 16.dp),
                    colorFilter = null
                )

                Text(
                    text = "Вход",
                    color = ColorUtils.PrimaryColor(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль", color = ColorUtils.GraySecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorUtils.TextFieldColor),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Заполните все поля"
                                return@clickable
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val result = authViewModel.login(email, password)
                                isLoading = false

                                if (result.isSuccess) {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                } else {
                                    errorMessage =
                                        result.exceptionOrNull()?.message ?: "Ошибка входа"
                                }
                            }
                        }
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ColorUtils.PrimaryColor()
                        )
                    } else {
                        Text(
                            text = "Продолжить",
                            color = ColorUtils.PrimaryColor(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    TextButton(
                        onClick = { navController.navigate(Screen.SignUp.route) },
                        modifier = Modifier.padding(top = 80.dp),
                    ) {
                        Text(
                            text = "Еще не зарегистрированы? Регистрация",
                            color = ColorUtils.GraySecondary
                        )
                    }
                }
            }
        }
    )
}