package com.example.hobbytracker.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AuthViewModel : ViewModel() {
    val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun createUserProfile(userId: String, email: String) {
        val userRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)

        val userData = hashMapOf(
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )

        userRef.set(userData)
            .addOnSuccessListener {
                Log.d("Auth", "Профиль пользователя создан")
            }
            .addOnFailureListener { e ->
                Log.e("Auth", "Ошибка создания профиля", e)
            }
    }

    fun signUp(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                user?.let {
                    val userData = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .await()

                    _isLoggedIn.value = true
                    onResult(true, null)
                } ?: run {
                    onResult(false, "Ошибка создания пользователя")
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                onResult(false, "Пароль должен содержать минимум 6 символов")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onResult(false, "Некорректный email")
            } catch (e: FirebaseAuthUserCollisionException) {
                onResult(false, "Пользователь уже существует")
            } catch (e: Exception) {
                onResult(false, "Ошибка регистрации: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoggedIn.value = true
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    data class UserData(
        val email: String,
        val firstName: String,
        val lastName: String,
        val profileImageUrl: String? = null
    )

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AuthViewModel", "Error loading user data", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    _userData.value = UserData(
                        email = it.getString("email") ?: "",
                        firstName = it.getString("firstName") ?: "",
                        lastName = it.getString("lastName") ?: "",
                        profileImageUrl = it.getString("profileImageUrl")
                    )
                }
            }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onComplete(false, "User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName
                )

                db.collection("users").document(user.uid)
                    .update(updates)
                    .await()

                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, "Error updating profile: ${e.message}")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _userData.value = null
    }

    fun uploadProfileImage(uri: Uri, onComplete: (String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(null)
            return
        }

        val storageRef = storage.reference
        val imageRef = storageRef.child("profile_images/${user.uid}/${UUID.randomUUID()}.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    db.collection("users").document(user.uid)
                        .update("profileImageUrl", downloadUri.toString())
                        .addOnSuccessListener {
                            onComplete(downloadUri.toString())
                        }
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }
}