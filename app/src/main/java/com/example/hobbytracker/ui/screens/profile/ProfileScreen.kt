package com.example.hobbytracker.ui.screens.profile

import android.Manifest
import android.util.Base64
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import androidx.compose.material.icons.filled.ArrowBack
import com.example.hobbytracker.ui.screens.profile.components.AvatarWithEdit
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val db = Firebase.firestore

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var avatarBase64 by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Обработчик выбора изображения
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                // Убираем переход к экрану обрезки
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Конвертируем в base64 и сохраняем
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                auth.currentUser?.uid?.let { userId ->
                    db.collection("users")
                        .document(userId)
                        .update("avatarBase64", base64)
                        .addOnSuccessListener {
                            avatarBase64 = base64
                        }
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки изображения"
            }
        }
    }

    // Обработчик результата обрезки
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val croppedBitmap by savedStateHandle
        ?.getStateFlow<Bitmap?>("cropped_bitmap", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }


    // Загрузка данных пользователя
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    firstName = doc.getString("firstName") ?: ""
                    lastName = doc.getString("lastName") ?: ""
                    avatarBase64 = doc.getString("avatarBase64")
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
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
            AvatarWithEdit(
                avatarBase64 = avatarBase64,
                initials = "${firstName.firstOrNull()}${lastName.firstOrNull()}",
                onEditClicked = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED) {
                        imagePicker.launch("image/*")
                    } else {
                        errorMessage = "Требуется разрешение на доступ к хранилищу"
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    auth.currentUser?.uid?.let { userId ->
                        db.collection("users")
                            .document(userId)
                            .update(
                                mapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName
                                )
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выйти", color = MaterialTheme.colorScheme.error)
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
