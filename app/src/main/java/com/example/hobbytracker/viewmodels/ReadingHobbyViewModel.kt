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

val Context.readingBookDataStore by preferencesDataStore("reading_book_prefs")

class ReadingBookViewModel : ViewModel() {
    private val _readingBookGoals = MutableStateFlow<List<String>>(emptyList())
    val readingBookGoals: StateFlow<List<String>> get() = _readingBookGoals

    private val _readingBookTasks = MutableStateFlow<List<Task>>(emptyList())
    val readingBookTasks: StateFlow<List<Task>> get() = _readingBookTasks

    private val _readingBookTotalDuration = MutableStateFlow(0)
    val readingBookTotalDuration: StateFlow<Int> get() = _readingBookTotalDuration

    private val _readingBookCellData = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
    val readingBookCellData: StateFlow<Map<Pair<Int, Int>, Int>> get() = _readingBookCellData

    private val _selectedReadingBookPeriod = MutableStateFlow("не задан")
    val selectedReadingBookPeriod: StateFlow<String> get() = _selectedReadingBookPeriod

    private fun readingBookCellIdentifier(period: String, row: Int, day: Int) = intPreferencesKey("readingBook_cell_$period$row$day")
    private val READING_BOOK_TOTAL_DURATION_KEY = intPreferencesKey("readingBook_total_duration")
    private val READING_BOOK_GOALS_KEY = stringPreferencesKey("readingBook_goals")
    private val READING_BOOK_TASKS_KEY = stringPreferencesKey("readingBook_tasks")
    private val READING_BOOK_PERIOD_KEY = stringPreferencesKey("readingBook_period")

    fun setupReadingBookDataStore(context: Context) {
        viewModelScope.launch {
            val preferences = context.readingBookDataStore.data.first()
            println("Initializing DataStore - Loaded preferences: $preferences")

            // Инициализация значений ячеек для текущего периода
            val currentPeriod = preferences[READING_BOOK_PERIOD_KEY] ?: "не задан"
            _selectedReadingBookPeriod.value = currentPeriod
            val cellData = buildMap {
                for (row in 0 until 5) {
                    for (day in 0 until 7) {
                        val value = if (currentPeriod != "не задан") {
                            preferences[readingBookCellIdentifier(currentPeriod, row, day)] ?: 0
                        } else 0
                        put(Pair(row, day), value)
                        println("Loaded cell $row,$day for period $currentPeriod with value $value")
                    }
                }
            }
            _readingBookCellData.value = cellData
            _readingBookTotalDuration.value = cellData.values.sum()
            println("Initialized total duration: ${_readingBookTotalDuration.value}")

            // Инициализация целей
            _readingBookGoals.value = preferences[READING_BOOK_GOALS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            println("Loaded goals: ${_readingBookGoals.value}")

            // Инициализация задач
            _readingBookTasks.value = preferences[READING_BOOK_TASKS_KEY]?.split(",")?.filter { it.isNotBlank() }
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
            println("Loaded tasks: ${_readingBookTasks.value}")
        }
    }

    fun addReadingBookGoal(goal: String) {
        if (goal.isNotBlank()) {
            val updatedGoals = _readingBookGoals.value.toMutableList().apply { add(goal) }
            _readingBookGoals.value = updatedGoals
            saveReadingBookGoals()
        }
    }

    fun removeReadingBookGoal(goal: String) {
        val updatedGoals = _readingBookGoals.value.filter { it != goal }
        _readingBookGoals.value = updatedGoals
        saveReadingBookGoals()
    }

    fun addReadingBookTask(task: String) {
        if (task.isNotBlank()) {
            val updatedTasks = _readingBookTasks.value.toMutableList().apply { add(Task(task, false)) }
            _readingBookTasks.value = updatedTasks
            saveReadingBookTasks()
        }
    }

    fun removeReadingBookTask(task: String) {
        val updatedTasks = _readingBookTasks.value.filter { it.text != task }
        _readingBookTasks.value = updatedTasks
        saveReadingBookTasks()
    }

    fun updateReadingBookTaskCompletion(task: String, isCompleted: Boolean) {
        val updatedTasks = _readingBookTasks.value.map { if (it.text == task) it.copy(isCompleted = isCompleted) else it }
        _readingBookTasks.value = updatedTasks
        saveReadingBookTasks()
    }

    fun getReadingBookCellValue(row: Int, day: Int): Int {
        return _readingBookCellData.value[Pair(row, day)] ?: 0
    }

    fun setReadingBookCellValue(row: Int, day: Int, minutes: Int) {
        viewModelScope.launch {
            val currentPeriod = _selectedReadingBookPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.readingBookDataStore?.edit { preferences ->
                val oldValue = preferences[readingBookCellIdentifier(currentPeriod, row, day)] ?: 0
                preferences[readingBookCellIdentifier(currentPeriod, row, day)] = minutes
                val newCellData = _readingBookCellData.value.toMutableMap().apply {
                    this[Pair(row, day)] = minutes
                }
                _readingBookCellData.value = newCellData
                val newTotalDuration = (_readingBookTotalDuration.value - oldValue + minutes).let { if (it < 0) 0 else it }
                _readingBookTotalDuration.value = newTotalDuration
                preferences[READING_BOOK_TOTAL_DURATION_KEY] = newTotalDuration
                println("Saved cell $row,$day for period $currentPeriod with value $minutes, new total duration: $newTotalDuration")
            }
        }
    }

    fun resetReadingBookCellValues() {
        viewModelScope.launch {
            val currentPeriod = _selectedReadingBookPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.readingBookDataStore?.edit { preferences ->
                val newCellData = buildMap {
                    for (row in 0 until 5) {
                        for (day in 0 until 7) {
                            preferences[readingBookCellIdentifier(currentPeriod, row, day)] = 0
                            put(Pair(row, day), 0)
                        }
                    }
                }
                _readingBookCellData.value = newCellData
                _readingBookTotalDuration.value = 0
                preferences[READING_BOOK_TOTAL_DURATION_KEY] = 0
                println("Reset all cells for period $currentPeriod and total duration to 0")
            }
        }
    }

    fun setSelectedReadingBookPeriod(period: String) {
        viewModelScope.launch {
            if (period != _selectedReadingBookPeriod.value) {
                _selectedReadingBookPeriod.value = period
                saveReadingBookPeriod()
                if (period != "не задан") {
                    initializePeriodData()
                } else {
                    _readingBookCellData.value = emptyMap()
                    _readingBookTotalDuration.value = 0
                }
            }
        }
    }

    internal suspend fun initializePeriodData() {
        val preferences = context?.readingBookDataStore?.data?.first() ?: return
        val cellData = buildMap {
            for (row in 0 until 5) {
                for (day in 0 until 7) {
                    val value = preferences[readingBookCellIdentifier(_selectedReadingBookPeriod.value, row, day)] ?: 0
                    put(Pair(row, day), value)
                    println("Initialized cell $row,$day for period ${_selectedReadingBookPeriod.value} with value $value")
                }
            }
        }
        _readingBookCellData.value = cellData
        _readingBookTotalDuration.value = cellData.values.sum()
        println("Initialized total duration for period ${_selectedReadingBookPeriod.value}: ${_readingBookTotalDuration.value}")
    }

    private fun saveReadingBookGoals() {
        viewModelScope.launch {
            context?.readingBookDataStore?.edit { preferences ->
                preferences[READING_BOOK_GOALS_KEY] = _readingBookGoals.value.joinToString(",")
                println("Saved goals: ${_readingBookGoals.value}")
            }
        }
    }

    private fun saveReadingBookTasks() {
        viewModelScope.launch {
            context?.readingBookDataStore?.edit { preferences ->
                preferences[READING_BOOK_TASKS_KEY] = _readingBookTasks.value.joinToString(",") { "${it.text}:${it.isCompleted}" }
                println("Saved tasks: ${_readingBookTasks.value}")
            }
        }
    }

    private fun saveReadingBookPeriod() {
        viewModelScope.launch {
            context?.readingBookDataStore?.edit { preferences ->
                preferences[READING_BOOK_PERIOD_KEY] = _selectedReadingBookPeriod.value
                println("Saved period: ${_selectedReadingBookPeriod.value}")
            }
        }
    }

    var context: Context? = null
}