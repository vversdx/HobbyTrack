package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.Image
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
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.viewmodels.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.util.ColorUtils
import com.example.hobbytracker.ui.theme.OutlinedText
import com.example.hobbytracker.ui.theme.Ribeye

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

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(270.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 56.dp)
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Логотип",
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(bottom = 8.dp),
                                colorFilter = null
                            )
                            OutlinedText(
                                textColor = Color.White,
                                text = "Регистрация",
                                outlineColor = ColorUtils.BluePrimary,
                                outlineWidth = 1.dp,
                                modifier = Modifier.padding(top = 8.dp) ,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Ribeye
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CustomOutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "Имя"
            )

            CustomOutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Фамилия"
            )

            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )

            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password
            )

            CustomOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Подтвердите пароль",
                visualTransformation = PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password
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

            TextButton(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Пароли не совпадают"
                        return@TextButton
                    }
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Заполните все поля"
                        return@TextButton
                    }
                    if (firstName.isBlank() || lastName.isBlank()) {
                        errorMessage = "Введите имя и фамилию"
                        return@TextButton
                    }

                    isLoading = true
                    authViewModel.signUp(email, password) { success, message ->
                        isLoading = false
                        if (success) {
                            authViewModel.updateUserProfile(firstName, lastName) { _, _ ->
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            }
                        } else {
                            errorMessage = message ?: "Ошибка регистрации"
                        }
                    }
                },
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF5883D4)
                    )
                } else {
                    OutlinedText(
                        text = "Зарегистрироваться",
                        textColor = Color.White,
                        outlineColor = ColorUtils.BluePrimary,
                        outlineWidth = 1.dp,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Ribeye
                        )
                    )

                }
            }
        }
    }
}

@Composable
private fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        singleLine = true,
        shape = MaterialTheme.shapes.small
    )
}