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

val Context.gamePlayDataStore by preferencesDataStore("game_play_prefs")

class GamePlayViewModel : ViewModel() {
    private val _gamePlayObjectives = MutableStateFlow<List<String>>(emptyList())
    val gamePlayObjectives: StateFlow<List<String>> get() = _gamePlayObjectives

    private val _gamePlayTasks = MutableStateFlow<List<Task>>(emptyList())
    val gamePlayTasks: StateFlow<List<Task>> get() = _gamePlayTasks

    private val _gamePlayTotalDuration = MutableStateFlow(0)
    val gamePlayTotalDuration: StateFlow<Int> get() = _gamePlayTotalDuration

    private val _gamePlayCellData = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
    val gamePlayCellData: StateFlow<Map<Pair<Int, Int>, Int>> get() = _gamePlayCellData

    private val _selectedGamePlayPeriod = MutableStateFlow("не задан")
    val selectedGamePlayPeriod: StateFlow<String> get() = _selectedGamePlayPeriod

    private fun gamePlayCellIdentifier(period: String, row: Int, day: Int) = intPreferencesKey("gamePlay_cell_$period$row$day")
    private val GAME_PLAY_TOTAL_DURATION_KEY = intPreferencesKey("gamePlay_total_duration")
    private val GAME_PLAY_OBJECTIVES_KEY = stringPreferencesKey("gamePlay_objectives")
    private val GAME_PLAY_TASKS_KEY = stringPreferencesKey("gamePlay_tasks")
    private val GAME_PLAY_PERIOD_KEY = stringPreferencesKey("gamePlay_period")

    fun setupGamePlayDataStore(context: Context) {
        viewModelScope.launch {
            val preferences = context.gamePlayDataStore.data.first()
            println("Initializing DataStore - Loaded preferences: $preferences")

            // Инициализация значений ячеек для текущего периода
            val currentPeriod = preferences[GAME_PLAY_PERIOD_KEY] ?: "не задан"
            _selectedGamePlayPeriod.value = currentPeriod
            val cellData = buildMap {
                for (row in 0 until 5) {
                    for (day in 0 until 7) {
                        val value = if (currentPeriod != "не задан") {
                            preferences[gamePlayCellIdentifier(currentPeriod, row, day)] ?: 0
                        } else 0
                        put(Pair(row, day), value)
                        println("Loaded cell $row,$day for period $currentPeriod with value $value")
                    }
                }
            }
            _gamePlayCellData.value = cellData
            _gamePlayTotalDuration.value = cellData.values.sum()
            println("Initialized total duration: ${_gamePlayTotalDuration.value}")

            // Инициализация целей
            _gamePlayObjectives.value = preferences[GAME_PLAY_OBJECTIVES_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            println("Loaded objectives: ${_gamePlayObjectives.value}")

            // Инициализация задач
            _gamePlayTasks.value = preferences[GAME_PLAY_TASKS_KEY]?.split(",")?.filter { it.isNotBlank() }
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
            println("Loaded tasks: ${_gamePlayTasks.value}")
        }
    }

    fun addGamePlayObjective(objective: String) {
        if (objective.isNotBlank()) {
            val updatedObjectives = _gamePlayObjectives.value.toMutableList().apply { add(objective) }
            _gamePlayObjectives.value = updatedObjectives
            saveGamePlayObjectives()
        }
    }

    fun removeGamePlayObjective(objective: String) {
        val updatedObjectives = _gamePlayObjectives.value.filter { it != objective }
        _gamePlayObjectives.value = updatedObjectives
        saveGamePlayObjectives()
    }

    fun addGamePlayTask(task: String) {
        if (task.isNotBlank()) {
            val updatedTasks = _gamePlayTasks.value.toMutableList().apply { add(Task(task, false)) }
            _gamePlayTasks.value = updatedTasks
            saveGamePlayTasks()
        }
    }

    fun removeGamePlayTask(task: String) {
        val updatedTasks = _gamePlayTasks.value.filter { it.text != task }
        _gamePlayTasks.value = updatedTasks
        saveGamePlayTasks()
    }

    fun updateGamePlayTaskCompletion(task: String, isCompleted: Boolean) {
        val updatedTasks = _gamePlayTasks.value.map { if (it.text == task) it.copy(isCompleted = isCompleted) else it }
        _gamePlayTasks.value = updatedTasks
        saveGamePlayTasks()
    }

    fun getGamePlayCellValue(row: Int, day: Int): Int {
        return _gamePlayCellData.value[Pair(row, day)] ?: 0
    }

    fun setGamePlayCellValue(row: Int, day: Int, minutes: Int) {
        viewModelScope.launch {
            val currentPeriod = _selectedGamePlayPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.gamePlayDataStore?.edit { preferences ->
                val oldValue = preferences[gamePlayCellIdentifier(currentPeriod, row, day)] ?: 0
                preferences[gamePlayCellIdentifier(currentPeriod, row, day)] = minutes
                val newCellData = _gamePlayCellData.value.toMutableMap().apply {
                    this[Pair(row, day)] = minutes
                }
                _gamePlayCellData.value = newCellData
                val newTotalDuration = (_gamePlayTotalDuration.value - oldValue + minutes).let { if (it < 0) 0 else it }
                _gamePlayTotalDuration.value = newTotalDuration
                preferences[GAME_PLAY_TOTAL_DURATION_KEY] = newTotalDuration
                println("Saved cell $row,$day for period $currentPeriod with value $minutes, new total duration: $newTotalDuration")
            }
        }
    }

    fun resetGamePlayCellValues() {
        viewModelScope.launch {
            val currentPeriod = _selectedGamePlayPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.gamePlayDataStore?.edit { preferences ->
                val newCellData = buildMap {
                    for (row in 0 until 5) {
                        for (day in 0 until 7) {
                            preferences[gamePlayCellIdentifier(currentPeriod, row, day)] = 0
                            put(Pair(row, day), 0)
                        }
                    }
                }
                _gamePlayCellData.value = newCellData
                _gamePlayTotalDuration.value = 0
                preferences[GAME_PLAY_TOTAL_DURATION_KEY] = 0
                println("Reset all cells for period $currentPeriod and total duration to 0")
            }
        }
    }

    fun setSelectedGamePlayPeriod(period: String) {
        viewModelScope.launch {
            if (period != _selectedGamePlayPeriod.value) {
                _selectedGamePlayPeriod.value = period
                saveGamePlayPeriod()
                if (period != "не задан") {
                    initializePeriodData()
                } else {
                    _gamePlayCellData.value = emptyMap()
                    _gamePlayTotalDuration.value = 0
                }
            }
        }
    }

    internal suspend fun initializePeriodData() {
        val preferences = context?.gamePlayDataStore?.data?.first() ?: return
        val cellData = buildMap {
            for (row in 0 until 5) {
                for (day in 0 until 7) {
                    val value = preferences[gamePlayCellIdentifier(_selectedGamePlayPeriod.value, row, day)] ?: 0
                    put(Pair(row, day), value)
                    println("Initialized cell $row,$day for period ${_selectedGamePlayPeriod.value} with value $value")
                }
            }
        }
        _gamePlayCellData.value = cellData
        _gamePlayTotalDuration.value = cellData.values.sum()
        println("Initialized total duration for period ${_selectedGamePlayPeriod.value}: ${_gamePlayTotalDuration.value}")
    }

    private fun saveGamePlayObjectives() {
        viewModelScope.launch {
            context?.gamePlayDataStore?.edit { preferences ->
                preferences[GAME_PLAY_OBJECTIVES_KEY] = _gamePlayObjectives.value.joinToString(",")
                println("Saved objectives: ${_gamePlayObjectives.value}")
            }
        }
    }

    private fun saveGamePlayTasks() {
        viewModelScope.launch {
            context?.gamePlayDataStore?.edit { preferences ->
                preferences[GAME_PLAY_TASKS_KEY] = _gamePlayTasks.value.joinToString(",") { "${it.text}:${it.isCompleted}" }
                println("Saved tasks: ${_gamePlayTasks.value}")
            }
        }
    }

    private fun saveGamePlayPeriod() {
        viewModelScope.launch {
            context?.gamePlayDataStore?.edit { preferences ->
                preferences[GAME_PLAY_PERIOD_KEY] = _selectedGamePlayPeriod.value
                println("Saved period: ${_selectedGamePlayPeriod.value}")
            }
        }
    }

    var context: Context? = null
}