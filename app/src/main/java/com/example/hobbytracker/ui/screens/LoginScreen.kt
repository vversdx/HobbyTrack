package com.example.hobbytracker.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbytracker.R
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme.isDarkTheme
import com.example.hobbytracker.util.ColorUtils
import com.example.hobbytracker.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { Log.d("UI_DEBUG", "Reset dialog dismissed"); showResetDialog = false },
            title = { Text("Восстановление пароля", color = Color.Black) },
            titleContentColor = Color.Black,
            text = {
                Column {
                    Text("Введите email для восстановления")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = email,
                        onValueChange = { Log.d("UI_DEBUG", "Email input changed: $it"); email = it },
                        label = { Text("Email") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("UI_DEBUG", "Reset password button clicked for email: $email")
                        if (email.isBlank()) {
                            Log.w("UI_DEBUG", "Empty email entered")
                            errorMessage = "Введите email"
                            return@Button
                        }

                        authViewModel.sendPasswordResetEmail(email) { success, error ->
                            if (success) {
                                Log.d("UI_DEBUG", "Success callback received")
                                showResetDialog = false
                                Toast.makeText(
                                    context,
                                    "Письмо отправлено на $email",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Log.e("UI_DEBUG", "Error callback: $error")
                                Toast.makeText(
                                    context,
                                    "Ошибка: ${error ?: "неизвестная"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Отправить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Log.d("UI_DEBUG", "Cancel button clicked")
                        showResetDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
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
                            .fillMaxWidth(0.9f)
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
                            .fillMaxWidth(0.9f)
                            .background(ColorUtils.TextFieldColor),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )

                    Text(
                        text = "Забыли пароль?",
                        color = ColorUtils.PrimaryColor(),
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { showResetDialog = true }
                            .padding(top = 8.dp)
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
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "еще не зарегистрированы?",
                        fontSize = 11.sp,
                        color = ColorUtils.GraySecondary
                    )
                    Text(
                        text = "Зарегистрироваться",
                        color = if (isDarkTheme) Color.White else Color(0xFF778BF9),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { navController.navigate(Screen.SignUp.route) }
                    )
                }
            }
        }
    )
}