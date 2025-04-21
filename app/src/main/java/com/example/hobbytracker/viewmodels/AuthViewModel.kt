package com.example.hobbytracker.viewmodels

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

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
}