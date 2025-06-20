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

val Context.artCraftDataStore by preferencesDataStore("art_craft_prefs")

class ArtCraftViewModel : ViewModel() {
    private val _artCraftObjectives = MutableStateFlow<List<String>>(emptyList())
    val artCraftObjectives: StateFlow<List<String>> get() = _artCraftObjectives

    private val _artCraftTasks = MutableStateFlow<List<Task>>(emptyList())
    val artCraftTasks: StateFlow<List<Task>> get() = _artCraftTasks

    private val _artCraftTotalDuration = MutableStateFlow(0)
    val artCraftTotalDuration: StateFlow<Int> get() = _artCraftTotalDuration

    private val _artCraftCellData = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
    val artCraftCellData: StateFlow<Map<Pair<Int, Int>, Int>> get() = _artCraftCellData

    private val _selectedArtCraftPeriod = MutableStateFlow("не задан")
    val selectedArtCraftPeriod: StateFlow<String> get() = _selectedArtCraftPeriod

    private fun artCraftCellIdentifier(period: String, row: Int, day: Int) = intPreferencesKey("artCraft_cell_$period$row$day")
    private val ART_CRAFT_TOTAL_DURATION_KEY = intPreferencesKey("artCraft_total_duration")
    private val ART_CRAFT_OBJECTIVES_KEY = stringPreferencesKey("artCraft_objectives")
    private val ART_CRAFT_TASKS_KEY = stringPreferencesKey("artCraft_tasks")
    private val ART_CRAFT_PERIOD_KEY = stringPreferencesKey("artCraft_period")

    fun setupArtCraftDataStore(context: Context) {
        viewModelScope.launch {
            val preferences = context.artCraftDataStore.data.first()
            println("Initializing DataStore - Loaded preferences: $preferences")

            // Инициализация значений ячеек для текущего периода
            val currentPeriod = preferences[ART_CRAFT_PERIOD_KEY] ?: "не задан"
            _selectedArtCraftPeriod.value = currentPeriod
            val cellData = buildMap {
                for (row in 0 until 5) {
                    for (day in 0 until 7) {
                        val value = if (currentPeriod != "не задан") {
                            preferences[artCraftCellIdentifier(currentPeriod, row, day)] ?: 0
                        } else 0
                        put(Pair(row, day), value)
                        println("Loaded cell $row,$day for period $currentPeriod with value $value")
                    }
                }
            }
            _artCraftCellData.value = cellData
            _artCraftTotalDuration.value = cellData.values.sum()
            println("Initialized total duration: ${_artCraftTotalDuration.value}")

            // Инициализация целей
            _artCraftObjectives.value = preferences[ART_CRAFT_OBJECTIVES_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            println("Loaded objectives: ${_artCraftObjectives.value}")

            // Инициализация задач
            _artCraftTasks.value = preferences[ART_CRAFT_TASKS_KEY]?.split(",")?.filter { it.isNotBlank() }
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
            println("Loaded tasks: ${_artCraftTasks.value}")
        }
    }

    fun addArtCraftObjective(objective: String) {
        if (objective.isNotBlank()) {
            val updatedObjectives = _artCraftObjectives.value.toMutableList().apply { add(objective) }
            _artCraftObjectives.value = updatedObjectives
            saveArtCraftObjectives()
        }
    }

    fun removeArtCraftObjective(objective: String) {
        val updatedObjectives = _artCraftObjectives.value.filter { it != objective }
        _artCraftObjectives.value = updatedObjectives
        saveArtCraftObjectives()
    }

    fun addArtCraftTask(task: String) {
        if (task.isNotBlank()) {
            val updatedTasks = _artCraftTasks.value.toMutableList().apply { add(Task(task, false)) }
            _artCraftTasks.value = updatedTasks
            saveArtCraftTasks()
        }
    }

    fun removeArtCraftTask(task: String) {
        val updatedTasks = _artCraftTasks.value.filter { it.text != task }
        _artCraftTasks.value = updatedTasks
        saveArtCraftTasks()
    }

    fun updateArtCraftTaskCompletion(task: String, isCompleted: Boolean) {
        val updatedTasks = _artCraftTasks.value.map { if (it.text == task) it.copy(isCompleted = isCompleted) else it }
        _artCraftTasks.value = updatedTasks
        saveArtCraftTasks()
    }

    fun getArtCraftCellValue(row: Int, day: Int): Int {
        return _artCraftCellData.value[Pair(row, day)] ?: 0
    }

    fun setArtCraftCellValue(row: Int, day: Int, minutes: Int) {
        viewModelScope.launch {
            val currentPeriod = _selectedArtCraftPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.artCraftDataStore?.edit { preferences ->
                val oldValue = preferences[artCraftCellIdentifier(currentPeriod, row, day)] ?: 0
                preferences[artCraftCellIdentifier(currentPeriod, row, day)] = minutes
                val newCellData = _artCraftCellData.value.toMutableMap().apply {
                    this[Pair(row, day)] = minutes
                }
                _artCraftCellData.value = newCellData
                val newTotalDuration = (_artCraftTotalDuration.value - oldValue + minutes).let { if (it < 0) 0 else it }
                _artCraftTotalDuration.value = newTotalDuration
                preferences[ART_CRAFT_TOTAL_DURATION_KEY] = newTotalDuration
                println("Saved cell $row,$day for period $currentPeriod with value $minutes, new total duration: $newTotalDuration")
            }
        }
    }

    fun resetArtCraftCellValues() {
        viewModelScope.launch {
            val currentPeriod = _selectedArtCraftPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.artCraftDataStore?.edit { preferences ->
                val newCellData = buildMap {
                    for (row in 0 until 5) {
                        for (day in 0 until 7) {
                            preferences[artCraftCellIdentifier(currentPeriod, row, day)] = 0
                            put(Pair(row, day), 0)
                        }
                    }
                }
                _artCraftCellData.value = newCellData
                _artCraftTotalDuration.value = 0
                preferences[ART_CRAFT_TOTAL_DURATION_KEY] = 0
                println("Reset all cells for period $currentPeriod and total duration to 0")
            }
        }
    }

    fun setSelectedArtCraftPeriod(period: String) {
        viewModelScope.launch {
            if (period != _selectedArtCraftPeriod.value) {
                _selectedArtCraftPeriod.value = period
                saveArtCraftPeriod()
                if (period != "не задан") {
                    initializePeriodData()
                } else {
                    _artCraftCellData.value = emptyMap()
                    _artCraftTotalDuration.value = 0
                }
            }
        }
    }

    internal suspend fun initializePeriodData() {
        val preferences = context?.artCraftDataStore?.data?.first() ?: return
        val cellData = buildMap {
            for (row in 0 until 5) {
                for (day in 0 until 7) {
                    val value = preferences[artCraftCellIdentifier(_selectedArtCraftPeriod.value, row, day)] ?: 0
                    put(Pair(row, day), value)
                    println("Initialized cell $row,$day for period ${_selectedArtCraftPeriod.value} with value $value")
                }
            }
        }
        _artCraftCellData.value = cellData
        _artCraftTotalDuration.value = cellData.values.sum()
        println("Initialized total duration for period ${_selectedArtCraftPeriod.value}: ${_artCraftTotalDuration.value}")
    }

    private fun saveArtCraftObjectives() {
        viewModelScope.launch {
            context?.artCraftDataStore?.edit { preferences ->
                preferences[ART_CRAFT_OBJECTIVES_KEY] = _artCraftObjectives.value.joinToString(",")
                println("Saved objectives: ${_artCraftObjectives.value}")
            }
        }
    }

    private fun saveArtCraftTasks() {
        viewModelScope.launch {
            context?.artCraftDataStore?.edit { preferences ->
                preferences[ART_CRAFT_TASKS_KEY] = _artCraftTasks.value.joinToString(",") { "${it.text}:${it.isCompleted}" }
                println("Saved tasks: ${_artCraftTasks.value}")
            }
        }
    }

    private fun saveArtCraftPeriod() {
        viewModelScope.launch {
            context?.artCraftDataStore?.edit { preferences ->
                preferences[ART_CRAFT_PERIOD_KEY] = _selectedArtCraftPeriod.value
                println("Saved period: ${_selectedArtCraftPeriod.value}")
            }
        }
    }

    var context: Context? = null
}