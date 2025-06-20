package com.example.hobbytracker.viewmodels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.hobbytracker.models.Task

val Context.dataStore by preferencesDataStore("music_hobby_prefs")

class MusicHobbyViewModel : ViewModel() {
    private val _musicHobbyGoals = MutableStateFlow<List<String>>(emptyList())
    val musicHobbyGoals: StateFlow<List<String>> get() = _musicHobbyGoals

    private val _musicHobbyTasks = MutableStateFlow<List<Task>>(emptyList())
    val musicHobbyTasks: StateFlow<List<Task>> get() = _musicHobbyTasks

    private val _musicHobbyTotalTime = MutableStateFlow(0)
    val musicHobbyTotalTime: StateFlow<Int> get() = _musicHobbyTotalTime

    private val _musicHobbyCellValues = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
    val musicHobbyCellValues: StateFlow<Map<Pair<Int, Int>, Int>> get() = _musicHobbyCellValues

    private val _selectedMusicHobbyMonth = MutableStateFlow("не задан")
    val selectedMusicHobbyMonth: StateFlow<String> get() = _selectedMusicHobbyMonth

    private fun musicHobbyCellKey(month: String, row: Int, day: Int) = intPreferencesKey("musicHobby_cell_$month}_${row}_${day}")
    private val MUSIC_HOBBY_TOTAL_TIME_KEY = intPreferencesKey("musicHobby_total_time")
    private val MUSIC_HOBBY_GOALS_KEY = stringPreferencesKey("musicHobby_goals")
    private val MUSIC_HOBBY_TASKS_KEY = stringPreferencesKey("musicHobby_tasks")
    private val MUSIC_HOBBY_MONTH_KEY = stringPreferencesKey("musicHobby_month")

    fun initializeMusicHobbyDataStore(context: Context) {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            println("Initializing DataStore - Loaded preferences: $preferences")

            // Инициализация значений ячеек для текущего месяца
            val currentMonth = preferences[MUSIC_HOBBY_MONTH_KEY] ?: "не задан"
            _selectedMusicHobbyMonth.value = currentMonth
            val cellValues = buildMap {
                for (row in 0 until 5) {
                    for (day in 0 until 7) {
                        val value = if (currentMonth != "не задан") {
                            preferences[musicHobbyCellKey(currentMonth, row, day)] ?: 0
                        } else 0
                        put(Pair(row, day), value)
                        println("Loaded cell $row,$day for month $currentMonth with value $value")
                    }
                }
            }
            _musicHobbyCellValues.value = cellValues
            _musicHobbyTotalTime.value = cellValues.values.sum()
            println("Initialized total time: ${_musicHobbyTotalTime.value}")

            // Инициализация целей
            _musicHobbyGoals.value = preferences[MUSIC_HOBBY_GOALS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            println("Loaded goals: ${_musicHobbyGoals.value}")

            // Инициализация задач
            _musicHobbyTasks.value = preferences[MUSIC_HOBBY_TASKS_KEY]?.split(",")?.filter { it.isNotBlank() }
                ?.map { taskStr ->
                    try {
                        val parts = taskStr.split(":")
                        val text = parts.getOrNull(0) ?: ""
                        val isCompletedStr = parts.getOrNull(1) ?: "false"
                        Task(text, isCompletedStr.toBoolean())
                    } catch (e: Exception) {
                        println("Error parsing task: $taskStr, using default values. Error: ${e.message}")
                        Task(taskStr, false)
                    }
                } ?: emptyList()
            println("Loaded tasks: ${_musicHobbyTasks.value}")
        }
    }

    fun addMusicHobbyGoal(goal: String) {
        if (goal.isNotBlank()) {
            val updatedGoals = _musicHobbyGoals.value.toMutableList().apply { add(goal) }
            _musicHobbyGoals.value = updatedGoals
            saveMusicHobbyGoals()
        }
    }

    fun removeMusicHobbyGoal(goal: String) {
        val updatedGoals = _musicHobbyGoals.value.filter { it != goal }
        _musicHobbyGoals.value = updatedGoals
        saveMusicHobbyGoals()
    }

    fun addMusicHobbyTask(task: String) {
        if (task.isNotBlank()) {
            val updatedTasks = _musicHobbyTasks.value.toMutableList().apply { add(Task(task, false)) }
            _musicHobbyTasks.value = updatedTasks
            saveMusicHobbyTasks()
        }
    }

    fun removeMusicHobbyTask(task: String) {
        val updatedTasks = _musicHobbyTasks.value.filter { it.text != task }
        _musicHobbyTasks.value = updatedTasks
        saveMusicHobbyTasks()
    }

    fun updateMusicHobbyTaskCompletion(task: String, isCompleted: Boolean) {
        val updatedTasks = _musicHobbyTasks.value.map { if (it.text == task) it.copy(isCompleted = isCompleted) else it }
        _musicHobbyTasks.value = updatedTasks
        saveMusicHobbyTasks()
    }

    fun getMusicHobbyCellValue(row: Int, day: Int): Int {
        return _musicHobbyCellValues.value[Pair(row, day)] ?: 0
    }

    fun setMusicHobbyCellValue(row: Int, day: Int, minutes: Int) {
        viewModelScope.launch {
            val currentMonth = _selectedMusicHobbyMonth.value
            if (currentMonth == "не задан") return@launch
            context?.dataStore?.edit { preferences ->
                val oldValue = preferences[musicHobbyCellKey(currentMonth, row, day)] ?: 0
                preferences[musicHobbyCellKey(currentMonth, row, day)] = minutes
                val newCellValues = _musicHobbyCellValues.value.toMutableMap().apply {
                    this[Pair(row, day)] = minutes
                }
                _musicHobbyCellValues.value = newCellValues
                val newTotalTime = (_musicHobbyTotalTime.value - oldValue + minutes).let { if (it < 0) 0 else it }
                _musicHobbyTotalTime.value = newTotalTime
                preferences[MUSIC_HOBBY_TOTAL_TIME_KEY] = newTotalTime
                println("Saved cell $row,$day for month $currentMonth with value $minutes, new total time: $newTotalTime")
            }
        }
    }

    fun resetMusicHobbyCellValues() {
        viewModelScope.launch {
            val currentMonth = _selectedMusicHobbyMonth.value
            if (currentMonth == "не задан") return@launch
            context?.dataStore?.edit { preferences ->
                val newCellValues = buildMap {
                    for (row in 0 until 5) {
                        for (day in 0 until 7) {
                            preferences[musicHobbyCellKey(currentMonth, row, day)] = 0
                            put(Pair(row, day), 0)
                        }
                    }
                }
                _musicHobbyCellValues.value = newCellValues
                _musicHobbyTotalTime.value = 0
                preferences[MUSIC_HOBBY_TOTAL_TIME_KEY] = 0
                println("Reset all cells for month $currentMonth and total time to 0")
            }
        }
    }

    fun setSelectedMusicHobbyMonth(month: String) {
        viewModelScope.launch {
            if (month != _selectedMusicHobbyMonth.value) {
                _selectedMusicHobbyMonth.value = month
                saveMusicHobbyMonth()
                if (month != "не задан") {
                    initializeMonthData()
                } else {
                    _musicHobbyCellValues.value = emptyMap()
                    _musicHobbyTotalTime.value = 0
                }
            }
        }
    }

    internal suspend fun initializeMonthData() {
        val preferences = context?.dataStore?.data?.first() ?: return
        val cellValues = buildMap {
            for (row in 0 until 5) {
                for (day in 0 until 7) {
                    val value = preferences[musicHobbyCellKey(_selectedMusicHobbyMonth.value, row, day)] ?: 0
                    put(Pair(row, day), value)
                    println("Initialized cell $row,$day for month ${_selectedMusicHobbyMonth.value} with value $value")
                }
            }
        }
        _musicHobbyCellValues.value = cellValues
        _musicHobbyTotalTime.value = cellValues.values.sum()
        println("Initialized total time for month ${_selectedMusicHobbyMonth.value}: ${_musicHobbyTotalTime.value}")
    }

    private fun saveMusicHobbyGoals() {
        viewModelScope.launch {
            context?.dataStore?.edit { preferences ->
                preferences[MUSIC_HOBBY_GOALS_KEY] = _musicHobbyGoals.value.joinToString(",")
                println("Saved goals: ${_musicHobbyGoals.value}")
            }
        }
    }

    private fun saveMusicHobbyTasks() {
        viewModelScope.launch {
            context?.dataStore?.edit { preferences ->
                preferences[MUSIC_HOBBY_TASKS_KEY] = _musicHobbyTasks.value.joinToString(",") { "${it.text}:${it.isCompleted}" }
                println("Saved tasks: ${_musicHobbyTasks.value}")
            }
        }
    }

    private fun saveMusicHobbyMonth() {
        viewModelScope.launch {
            context?.dataStore?.edit { preferences ->
                preferences[MUSIC_HOBBY_MONTH_KEY] = _selectedMusicHobbyMonth.value
                println("Saved month: ${_selectedMusicHobbyMonth.value}")
            }
        }
    }

    var context: Context? = null
}