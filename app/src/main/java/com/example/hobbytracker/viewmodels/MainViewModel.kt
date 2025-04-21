package com.example.hobbytracker.viewmodels

import com.example.hobbytracker.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbytracker.data.HobbyCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel : ViewModel() {
    // Инициализация Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null

    // Состояния
    private val _categories = MutableStateFlow<List<HobbyCategory>>(emptyList())
    val categories: StateFlow<List<HobbyCategory>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadInitialCategories()
        setupHobbyCountListener()
    }

    override fun onCleared() {
        snapshotListener?.remove()
        super.onCleared()
    }

    private fun loadInitialCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Стартовый список категорий (можно заменить на загрузку из Firestore)
                _categories.value = listOf(
                    HobbyCategory(id = "1", name = "Музыка", iconRes = R.drawable.ic_music),
                    HobbyCategory(id = "2", name = "Искусство", iconRes = R.drawable.ic_art),
                    HobbyCategory(id = "3", name = "Спорт", iconRes = R.drawable.ic_sport),
                    HobbyCategory(id = "4", name = "Чтение", iconRes = R.drawable.ic_reading),
                    HobbyCategory(id = "5", name = "Игры", iconRes = R.drawable.ic_games),
                    HobbyCategory(id = "6", name = "Другое", iconRes = R.drawable.ic_other)
                )
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки категорий: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setupHobbyCountListener() {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        snapshotListener = db.collection("users")
            .document(userId)
            .collection("hobbies")
            .addSnapshotListener { snapshot, firestoreError ->
                if (firestoreError != null) {
                    _error.value = "Ошибка слушателя: ${firestoreError.message}"
                    return@addSnapshotListener
                }

                // Группируем хобби по categoryId и подсчитываем количество
                val countsMap = snapshot?.documents
                    ?.groupBy { it.getString("categoryId") ?: "" }
                    ?.mapValues { it.value.size }
                    ?: emptyMap()

                // Обновляем счетчики в категориях
                _categories.update { currentCategories ->
                    currentCategories.map { category ->
                        category.copy(hobbyCount = countsMap[category.id] ?: 0)
                    }
                }
            }
    }

    fun refreshData() {
        loadInitialCategories()
    }
}