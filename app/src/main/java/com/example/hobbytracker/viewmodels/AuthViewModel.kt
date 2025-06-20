package com.example.hobbytracker.viewmodels

import android.content.Context
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
import java.io.File
import java.util.UUID

class AuthViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    sealed class AuthState {
        object Unauthenticated : AuthState()
        data class Authenticated(val userId: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let { user ->
                _authState.value = AuthState.Authenticated(user.uid)
            } ?: run {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User creation failed")
            val userData = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email
            )

            db.collection("users").document(userId).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        Log.d("AUTH_DEBUG", "▶️ Starting password reset for: $email")

        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AUTH_DEBUG", "✅ Password reset email sent successfully")
                        onResult(true, null)
                    } else {
                        val error = task.exception?.apply {
                            Log.e("AUTH_DEBUG", "❗ Firebase error: ${this.javaClass.simpleName}", this)
                        }
                        val errorMsg = error?.message ?: "Unknown error"
                        Log.e("AUTH_DEBUG", "❌ Failed to send reset email: $errorMsg")
                        onResult(false, errorMsg)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AUTH_DEBUG", "🔥 Critical failure", e)
                    onResult(false, "Critical error: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "⚡ Exception caught!", e)
            onResult(false, "Exception: ${e.message}")
        }
    }

}