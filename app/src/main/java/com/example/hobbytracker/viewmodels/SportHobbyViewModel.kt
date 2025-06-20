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

val Context.sportActivityDataStore by preferencesDataStore("sport_activity_prefs")

class SportActivityViewModel : ViewModel() {
    private val _sportActivityTargets = MutableStateFlow<List<String>>(emptyList())
    val sportActivityTargets: StateFlow<List<String>> get() = _sportActivityTargets

    private val _sportActivityTasks = MutableStateFlow<List<Task>>(emptyList())
    val sportActivityTasks: StateFlow<List<Task>> get() = _sportActivityTasks

    private val _sportActivityTotalDuration = MutableStateFlow(0)
    val sportActivityTotalDuration: StateFlow<Int> get() = _sportActivityTotalDuration

    private val _sportActivityCellData = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
    val sportActivityCellData: StateFlow<Map<Pair<Int, Int>, Int>> get() = _sportActivityCellData

    private val _selectedSportActivityPeriod = MutableStateFlow("не задан")
    val selectedSportActivityPeriod: StateFlow<String> get() = _selectedSportActivityPeriod

    private fun sportActivityCellIdentifier(period: String, row: Int, day: Int) = intPreferencesKey("sportActivity_cell_$period$row$day")
    private val SPORT_ACTIVITY_TOTAL_DURATION_KEY = intPreferencesKey("sportActivity_total_duration")
    private val SPORT_ACTIVITY_TARGETS_KEY = stringPreferencesKey("sportActivity_targets")
    private val SPORT_ACTIVITY_TASKS_KEY = stringPreferencesKey("sportActivity_tasks")
    private val SPORT_ACTIVITY_PERIOD_KEY = stringPreferencesKey("sportActivity_period")

    fun setupSportActivityDataStore(context: Context) {
        viewModelScope.launch {
            val preferences = context.sportActivityDataStore.data.first()
            println("Initializing DataStore - Loaded preferences: $preferences")

            // Инициализация значений ячеек для текущего периода
            val currentPeriod = preferences[SPORT_ACTIVITY_PERIOD_KEY] ?: "не задан"
            _selectedSportActivityPeriod.value = currentPeriod
            val cellData = buildMap {
                for (row in 0 until 5) {
                    for (day in 0 until 7) {
                        val value = if (currentPeriod != "не задан") {
                            preferences[sportActivityCellIdentifier(currentPeriod, row, day)] ?: 0
                        } else 0
                        put(Pair(row, day), value)
                        println("Loaded cell $row,$day for period $currentPeriod with value $value")
                    }
                }
            }
            _sportActivityCellData.value = cellData
            _sportActivityTotalDuration.value = cellData.values.sum()
            println("Initialized total duration: ${_sportActivityTotalDuration.value}")

            // Инициализация целей
            _sportActivityTargets.value = preferences[SPORT_ACTIVITY_TARGETS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            println("Loaded targets: ${_sportActivityTargets.value}")

            // Инициализация задач
            _sportActivityTasks.value = preferences[SPORT_ACTIVITY_TASKS_KEY]?.split(",")?.filter { it.isNotBlank() }
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
            println("Loaded tasks: ${_sportActivityTasks.value}")
        }
    }

    fun addSportActivityTarget(target: String) {
        if (target.isNotBlank()) {
            val updatedTargets = _sportActivityTargets.value.toMutableList().apply { add(target) }
            _sportActivityTargets.value = updatedTargets
            saveSportActivityTargets()
        }
    }

    fun removeSportActivityTarget(target: String) {
        val updatedTargets = _sportActivityTargets.value.filter { it != target }
        _sportActivityTargets.value = updatedTargets
        saveSportActivityTargets()
    }

    fun addSportActivityTask(task: String) {
        if (task.isNotBlank()) {
            val updatedTasks = _sportActivityTasks.value.toMutableList().apply { add(Task(task, false)) }
            _sportActivityTasks.value = updatedTasks
            saveSportActivityTasks()
        }
    }

    fun removeSportActivityTask(task: String) {
        val updatedTasks = _sportActivityTasks.value.filter { it.text != task }
        _sportActivityTasks.value = updatedTasks
        saveSportActivityTasks()
    }

    fun updateSportActivityTaskCompletion(task: String, isCompleted: Boolean) {
        val updatedTasks = _sportActivityTasks.value.map { if (it.text == task) it.copy(isCompleted = isCompleted) else it }
        _sportActivityTasks.value = updatedTasks
        saveSportActivityTasks()
    }

    fun getSportActivityCellValue(row: Int, day: Int): Int {
        return _sportActivityCellData.value[Pair(row, day)] ?: 0
    }

    fun setSportActivityCellValue(row: Int, day: Int, minutes: Int) {
        viewModelScope.launch {
            val currentPeriod = _selectedSportActivityPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.sportActivityDataStore?.edit { preferences ->
                val oldValue = preferences[sportActivityCellIdentifier(currentPeriod, row, day)] ?: 0
                preferences[sportActivityCellIdentifier(currentPeriod, row, day)] = minutes
                val newCellData = _sportActivityCellData.value.toMutableMap().apply {
                    this[Pair(row, day)] = minutes
                }
                _sportActivityCellData.value = newCellData
                val newTotalDuration = (_sportActivityTotalDuration.value - oldValue + minutes).let { if (it < 0) 0 else it }
                _sportActivityTotalDuration.value = newTotalDuration
                preferences[SPORT_ACTIVITY_TOTAL_DURATION_KEY] = newTotalDuration
                println("Saved cell $row,$day for period $currentPeriod with value $minutes, new total duration: $newTotalDuration")
            }
        }
    }

    fun resetSportActivityCellValues() {
        viewModelScope.launch {
            val currentPeriod = _selectedSportActivityPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.sportActivityDataStore?.edit { preferences ->
                val newCellData = buildMap {
                    for (row in 0 until 5) {
                        for (day in 0 until 7) {
                            preferences[sportActivityCellIdentifier(currentPeriod, row, day)] = 0
                            put(Pair(row, day), 0)
                        }
                    }
                }
                _sportActivityCellData.value = newCellData
                _sportActivityTotalDuration.value = 0
                preferences[SPORT_ACTIVITY_TOTAL_DURATION_KEY] = 0
                println("Reset all cells for period $currentPeriod and total duration to 0")
            }
        }
    }

    fun setSelectedSportActivityPeriod(period: String) {
        viewModelScope.launch {
            if (period != _selectedSportActivityPeriod.value) {
                _selectedSportActivityPeriod.value = period
                saveSportActivityPeriod()
                if (period != "не задан") {
                    initializePeriodData()
                } else {
                    _sportActivityCellData.value = emptyMap()
                    _sportActivityTotalDuration.value = 0
                }
            }
        }
    }

    internal suspend fun initializePeriodData() {
        val preferences = context?.sportActivityDataStore?.data?.first() ?: return
        val cellData = buildMap {
            for (row in 0 until 5) {
                for (day in 0 until 7) {
                    val value = preferences[sportActivityCellIdentifier(_selectedSportActivityPeriod.value, row, day)] ?: 0
                    put(Pair(row, day), value)
                    println("Initialized cell $row,$day for period ${_selectedSportActivityPeriod.value} with value $value")
                }
            }
        }
        _sportActivityCellData.value = cellData
        _sportActivityTotalDuration.value = cellData.values.sum()
        println("Initialized total duration for period ${_selectedSportActivityPeriod.value}: ${_sportActivityTotalDuration.value}")
    }

    private fun saveSportActivityTargets() {
        viewModelScope.launch {
            context?.sportActivityDataStore?.edit { preferences ->
                preferences[SPORT_ACTIVITY_TARGETS_KEY] = _sportActivityTargets.value.joinToString(",")
                println("Saved targets: ${_sportActivityTargets.value}")
            }
        }
    }

    private fun saveSportActivityTasks() {
        viewModelScope.launch {
            context?.sportActivityDataStore?.edit { preferences ->
                preferences[SPORT_ACTIVITY_TASKS_KEY] = _sportActivityTasks.value.joinToString(",") { "${it.text}:${it.isCompleted}" }
                println("Saved tasks: ${_sportActivityTasks.value}")
            }
        }
    }

    private fun saveSportActivityPeriod() {
        viewModelScope.launch {
            context?.sportActivityDataStore?.edit { preferences ->
                preferences[SPORT_ACTIVITY_PERIOD_KEY] = _selectedSportActivityPeriod.value
                println("Saved period: ${_selectedSportActivityPeriod.value}")
            }
        }
    }

    var context: Context? = null
}