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

val Context.otherInterestDataStore by preferencesDataStore("other_interest_prefs")

class OtherInterestViewModel : ViewModel() {
    private val _otherInterestAims = MutableStateFlow<List<String>>(emptyList())
    val otherInterestAims: StateFlow<List<String>> get() = _otherInterestAims

    private val _otherInterestTasks = MutableStateFlow<List<Task>>(emptyList())
    val otherInterestTasks: StateFlow<List<Task>> get() = _otherInterestTasks

    private val _otherInterestTotalDuration = MutableStateFlow(0)
    val otherInterestTotalDuration: StateFlow<Int> get() = _otherInterestTotalDuration

    private val _otherInterestCellData = MutableStateFlow<Map<Pair<Int, Int>, Int>>(emptyMap())
    val otherInterestCellData: StateFlow<Map<Pair<Int, Int>, Int>> get() = _otherInterestCellData

    private val _selectedOtherInterestPeriod = MutableStateFlow("не задан")
    val selectedOtherInterestPeriod: StateFlow<String> get() = _selectedOtherInterestPeriod

    private fun otherInterestCellIdentifier(period: String, row: Int, day: Int) = intPreferencesKey("otherInterest_cell_$period$row$day")
    private val OTHER_INTEREST_TOTAL_DURATION_KEY = intPreferencesKey("otherInterest_total_duration")
    private val OTHER_INTEREST_AIMS_KEY = stringPreferencesKey("otherInterest_aims")
    private val OTHER_INTEREST_TASKS_KEY = stringPreferencesKey("otherInterest_tasks")
    private val OTHER_INTEREST_PERIOD_KEY = stringPreferencesKey("otherInterest_period")

    fun setupOtherInterestDataStore(context: Context) {
        viewModelScope.launch {
            val preferences = context.otherInterestDataStore.data.first()
            println("Initializing DataStore - Loaded preferences: $preferences")

            // Инициализация значений ячеек для текущего периода
            val currentPeriod = preferences[OTHER_INTEREST_PERIOD_KEY] ?: "не задан"
            _selectedOtherInterestPeriod.value = currentPeriod
            val cellData = buildMap {
                for (row in 0 until 5) {
                    for (day in 0 until 7) {
                        val value = if (currentPeriod != "не задан") {
                            preferences[otherInterestCellIdentifier(currentPeriod, row, day)] ?: 0
                        } else 0
                        put(Pair(row, day), value)
                        println("Loaded cell $row,$day for period $currentPeriod with value $value")
                    }
                }
            }
            _otherInterestCellData.value = cellData
            _otherInterestTotalDuration.value = cellData.values.sum()
            println("Initialized total duration: ${_otherInterestTotalDuration.value}")

            // Инициализация целей
            _otherInterestAims.value = preferences[OTHER_INTEREST_AIMS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            println("Loaded aims: ${_otherInterestAims.value}")

            // Инициализация задач
            _otherInterestTasks.value = preferences[OTHER_INTEREST_TASKS_KEY]?.split(",")?.filter { it.isNotBlank() }
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
            println("Loaded tasks: ${_otherInterestTasks.value}")
        }
    }

    fun addOtherInterestAim(aim: String) {
        if (aim.isNotBlank()) {
            val updatedAims = _otherInterestAims.value.toMutableList().apply { add(aim) }
            _otherInterestAims.value = updatedAims
            saveOtherInterestAims()
        }
    }

    fun removeOtherInterestAim(aim: String) {
        val updatedAims = _otherInterestAims.value.filter { it != aim }
        _otherInterestAims.value = updatedAims
        saveOtherInterestAims()
    }

    fun addOtherInterestTask(task: String) {
        if (task.isNotBlank()) {
            val updatedTasks = _otherInterestTasks.value.toMutableList().apply { add(Task(task, false)) }
            _otherInterestTasks.value = updatedTasks
            saveOtherInterestTasks()
        }
    }

    fun removeOtherInterestTask(task: String) {
        val updatedTasks = _otherInterestTasks.value.filter { it.text != task }
        _otherInterestTasks.value = updatedTasks
        saveOtherInterestTasks()
    }

    fun updateOtherInterestTaskCompletion(task: String, isCompleted: Boolean) {
        val updatedTasks = _otherInterestTasks.value.map { if (it.text == task) it.copy(isCompleted = isCompleted) else it }
        _otherInterestTasks.value = updatedTasks
        saveOtherInterestTasks()
    }

    fun getOtherInterestCellValue(row: Int, day: Int): Int {
        return _otherInterestCellData.value[Pair(row, day)] ?: 0
    }

    fun setOtherInterestCellValue(row: Int, day: Int, minutes: Int) {
        viewModelScope.launch {
            val currentPeriod = _selectedOtherInterestPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.otherInterestDataStore?.edit { preferences ->
                val oldValue = preferences[otherInterestCellIdentifier(currentPeriod, row, day)] ?: 0
                preferences[otherInterestCellIdentifier(currentPeriod, row, day)] = minutes
                val newCellData = _otherInterestCellData.value.toMutableMap().apply {
                    this[Pair(row, day)] = minutes
                }
                _otherInterestCellData.value = newCellData
                val newTotalDuration = (_otherInterestTotalDuration.value - oldValue + minutes).let { if (it < 0) 0 else it }
                _otherInterestTotalDuration.value = newTotalDuration
                preferences[OTHER_INTEREST_TOTAL_DURATION_KEY] = newTotalDuration
                println("Saved cell $row,$day for period $currentPeriod with value $minutes, new total duration: $newTotalDuration")
            }
        }
    }

    fun resetOtherInterestCellValues() {
        viewModelScope.launch {
            val currentPeriod = _selectedOtherInterestPeriod.value
            if (currentPeriod == "не задан") return@launch
            context?.otherInterestDataStore?.edit { preferences ->
                val newCellData = buildMap {
                    for (row in 0 until 5) {
                        for (day in 0 until 7) {
                            preferences[otherInterestCellIdentifier(currentPeriod, row, day)] = 0
                            put(Pair(row, day), 0)
                        }
                    }
                }
                _otherInterestCellData.value = newCellData
                _otherInterestTotalDuration.value = 0
                preferences[OTHER_INTEREST_TOTAL_DURATION_KEY] = 0
                println("Reset all cells for period $currentPeriod and total duration to 0")
            }
        }
    }

    fun setSelectedOtherInterestPeriod(period: String) {
        viewModelScope.launch {
            if (period != _selectedOtherInterestPeriod.value) {
                _selectedOtherInterestPeriod.value = period
                saveOtherInterestPeriod()
                if (period != "не задан") {
                    initializePeriodData()
                } else {
                    _otherInterestCellData.value = emptyMap()
                    _otherInterestTotalDuration.value = 0
                }
            }
        }
    }

    internal suspend fun initializePeriodData() {
        val preferences = context?.otherInterestDataStore?.data?.first() ?: return
        val cellData = buildMap {
            for (row in 0 until 5) {
                for (day in 0 until 7) {
                    val value = preferences[otherInterestCellIdentifier(_selectedOtherInterestPeriod.value, row, day)] ?: 0
                    put(Pair(row, day), value)
                    println("Initialized cell $row,$day for period ${_selectedOtherInterestPeriod.value} with value $value")
                }
            }
        }
        _otherInterestCellData.value = cellData
        _otherInterestTotalDuration.value = cellData.values.sum()
        println("Initialized total duration for period ${_selectedOtherInterestPeriod.value}: ${_otherInterestTotalDuration.value}")
    }

    private fun saveOtherInterestAims() {
        viewModelScope.launch {
            context?.otherInterestDataStore?.edit { preferences ->
                preferences[OTHER_INTEREST_AIMS_KEY] = _otherInterestAims.value.joinToString(",")
                println("Saved aims: ${_otherInterestAims.value}")
            }
        }
    }

    private fun saveOtherInterestTasks() {
        viewModelScope.launch {
            context?.otherInterestDataStore?.edit { preferences ->
                preferences[OTHER_INTEREST_TASKS_KEY] = _otherInterestTasks.value.joinToString(",") { "${it.text}:${it.isCompleted}" }
                println("Saved tasks: ${_otherInterestTasks.value}")
            }
        }
    }

    private fun saveOtherInterestPeriod() {
        viewModelScope.launch {
            context?.otherInterestDataStore?.edit { preferences ->
                preferences[OTHER_INTEREST_PERIOD_KEY] = _selectedOtherInterestPeriod.value
                println("Saved period: ${_selectedOtherInterestPeriod.value}")
            }
        }
    }

    var context: Context? = null
}