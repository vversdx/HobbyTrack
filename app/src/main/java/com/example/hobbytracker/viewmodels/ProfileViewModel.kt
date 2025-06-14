package com.example.hobbytracker.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbytracker.ui.screens.profile.ProfileState
import com.example.hobbytracker.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    fun handleImageSelection(context: Context, uri: Uri) {
        viewModelScope.launch {
            uploadProfileImage(context, uri)
        }
    }

    fun loadProfile(context: Context) {
        val userId = auth.currentUser?.uid ?: run {
            _state.value = _state.value.copy(error = "Пользователь не авторизован")
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(userId).get().await()

                if (!userDoc.exists()) {
                    _state.value = _state.value.copy(
                        error = "Данные пользователя не найдены",
                        isLoading = false
                    )
                    return@launch
                }

                _state.value = ProfileState(
                    firstName = userDoc.getString("firstName") ?: "",
                    lastName = userDoc.getString("lastName") ?: "",
                    middleName = userDoc.getString("middleName"),
                    phone = userDoc.getString("phone"),
                    email = auth.currentUser?.email ?: "",
                    photoUrl = userDoc.getString("photoUrl"),
                    profileImage = loadProfileImage(context, userId, userDoc.getString("photoUrl")),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Ошибка загрузки: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadProfileImage(context: Context, userId: String, photoUrl: String?): Bitmap? {
        return try {
            ImageUtils.loadBitmapFromCache(context, userId) ?: photoUrl?.let { url ->
                ImageUtils.loadImageFromUrl(url)?.also {
                    ImageUtils.saveBitmapToCache(context, it, userId)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        middleName: String?,
        phone: String?
    ) {
        val userId = auth.currentUser?.uid ?: return

        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val updates = mapOf<String, Any>(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "middleName" to (middleName ?: ""),
                    "phone" to (phone ?: "")
                )

                db.collection("users").document(userId).update(updates).await()

                _state.value = _state.value.copy(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = middleName,
                    phone = phone,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Ошибка сохранения: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    suspend fun uploadProfileImage(context: Context, uri: Uri): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            // Загрузка изображения
            val bitmap = ImageUtils.uriToBitmap(context, uri)
            bitmap?.let {
                // Сохраняем в кэш сразу
                ImageUtils.saveBitmapToCache(context, it, userId)

                // Обновляем состояние ДО загрузки на сервер
                _state.value = _state.value.copy(
                    profileImage = it
                )
            }

            // Загрузка в Storage
            val ref = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()

            // Получение URL
            val url = ref.downloadUrl.await().toString()

            // Обновление Firestore
            db.collection("users").document(userId).update("photoUrl", url).await()

            // Обновление состояния с URL
            _state.value = _state.value.copy(
                photoUrl = url
            )

            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Ошибка загрузки фото: ${e.localizedMessage}")
            false
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}