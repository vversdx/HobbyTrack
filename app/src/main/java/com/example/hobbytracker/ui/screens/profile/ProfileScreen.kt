package com.example.hobbytracker.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hobbytracker.ui.screens.imagecropper.ImageCropperScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageCropper by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                tempImageUri = it
                showImageCropper = true
            }
        }
    )

    LaunchedEffect(Unit) {
        try {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                val document = Firebase.firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (document.exists()) {
                    firstName = document.getString("firstName") ?: "Имя"
                    lastName = document.getString("lastName") ?: "Фамилия"
                }
            } else {
                errorMessage = "Пользователь не авторизован"
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (showImageCropper && tempImageUri != null) {
        ImageCropperScreen(
            imageUri = tempImageUri.toString(),
            onComplete = { bitmap ->
                showImageCropper = false
                // Здесь можно сохранить bitmap в Storage
            },
            onBack = { showImageCropper = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Профиль") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                } else {
                    // Блок ошибок
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(120.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(120.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${firstName.firstOrNull()?.uppercase()}${lastName.firstOrNull()?.uppercase()}",
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                        }
                        IconButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Изменить аватар")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Имя") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Фамилия") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (firstName.isBlank() || lastName.isBlank()) {
                                    errorMessage = "Заполните все поля"
                                    return@Button
                                }

                                val currentUser = Firebase.auth.currentUser
                                if (currentUser != null) {
                                    val userData = hashMapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName
                                    )

                                    Firebase.firestore.collection("users")
                                        .document(currentUser.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            errorMessage = null
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Ошибка сохранения: ${e.message}"
                                        }
                                } else {
                                    errorMessage = "Пользователь не авторизован"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp))
                        {
                            Text("Сохранить изменения", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = {
                                Firebase.auth.signOut()
                                navController.navigate("login") { popUpTo(0) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Выйти из аккаунта", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}