package com.example.hobbytracker.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.hobbytracker.R
import com.example.hobbytracker.ui.components.NewTopAppBar
import com.example.hobbytracker.viewmodels.SportActivityViewModel
import com.example.hobbytracker.models.Task
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.zIndex
import com.example.hobbytracker.util.ColorUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

@Composable
fun SportHobbyScreen(navController: NavController) {
    val viewModel: SportActivityViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (viewModel.context == null) {
            viewModel.context = context
            viewModel.setupSportActivityDataStore(context)
            if (viewModel.selectedSportActivityPeriod.value != "не задан") {
                viewModel.initializePeriodData()
            }
        }
    }
    val sportActivityTargets by viewModel.sportActivityTargets.collectAsState()
    val sportActivityTasks by viewModel.sportActivityTasks.collectAsState()
    val sportActivityCellData by viewModel.sportActivityCellData.collectAsState()
    val selectedSportActivityPeriod by viewModel.selectedSportActivityPeriod.collectAsState()
    val (showSportActivityDialog, setShowSportActivityDialog) = remember { mutableStateOf(false) }
    val (newSportActivityTarget, setNewSportActivityTarget) = remember { mutableStateOf("") }
    val (showSportActivityTaskDialog, setShowSportActivityTaskDialog) = remember { mutableStateOf(false) }
    val (newSportActivityTask, setNewSportActivityTask) = remember { mutableStateOf("") }

    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val sportActivityTotalDuration by viewModel.sportActivityTotalDuration.collectAsState()
    val (selectedSportActivityCell, setSelectedSportActivityCell) = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val (showSportActivityTimeDialog, setShowSportActivityTimeDialog) = remember { mutableStateOf(false) }
    val (selectedSportActivityTime, setSelectedSportActivityTime) = remember { mutableStateOf("0:0") }
    val (sportActivityPeriodDropdownExpanded, setSportActivityPeriodDropdownExpanded) = remember { mutableStateOf(false) }
    val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

    Scaffold(
        modifier = Modifier.zIndex(0f),
        topBar = { Box(modifier = Modifier.height(48.dp)) },
        floatingActionButton = {}
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Спорт",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = ColorUtils.HobbyCategoryColor(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 60.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    TargetsSection(
                        targets = sportActivityTargets,
                        viewModel = viewModel,
                        showDialog = showSportActivityDialog,
                        onAddClick = { setShowSportActivityDialog(true) },
                        newTarget = newSportActivityTarget,
                        onNewTargetChange = { setNewSportActivityTarget(it) },
                        onRemoveClick = viewModel::removeSportActivityTarget,
                        onDialogDismiss = { setShowSportActivityDialog(false) }
                    )
                }
                item {
                    TasksSection(
                        tasks = sportActivityTasks,
                        viewModel = viewModel,
                        showDialog = showSportActivityTaskDialog,
                        onAddClick = { setShowSportActivityTaskDialog(true) },
                        newTask = newSportActivityTask,
                        onNewTaskChange = { setNewSportActivityTask(it) },
                        onRemoveClick = viewModel::removeSportActivityTask,
                        onDialogDismiss = { setShowSportActivityTaskDialog(false) },
                        onTaskCheckedChange = viewModel::updateSportActivityTaskCompletion
                    )
                }
                item {
                    CalendarSection(
                        daysOfWeek = daysOfWeek,
                        selectedPeriod = selectedSportActivityPeriod,
                        onPeriodChange = { viewModel.setSelectedSportActivityPeriod(it) },
                        cellData = sportActivityCellData,
                        viewModel = viewModel,
                        onCellClick = { dayIndex, rowIndex ->
                            val value = viewModel.getSportActivityCellValue(rowIndex, dayIndex)
                            if (value == 0) {
                                setSelectedSportActivityCell(Pair(rowIndex, dayIndex))
                                setShowSportActivityTimeDialog(true)
                            } else {
                                viewModel.setSportActivityCellValue(rowIndex, dayIndex, 0)
                            }
                        },
                        periodDropdownExpanded = sportActivityPeriodDropdownExpanded,
                        onPeriodDropdownChange = { setSportActivityPeriodDropdownExpanded(it) }
                    )
                }
                item {
                    Text(
                        text = "Сумарное время: ${sportActivityTotalDuration / 60}:${sportActivityTotalDuration % 60}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        ),
                        modifier = Modifier.padding(top = 8.dp).padding(horizontal = 10.dp)
                    )
                }
            }
        }

        if (showSportActivityTimeDialog && selectedSportActivityCell != null) {
            AlertDialog(
                onDismissRequest = { setShowSportActivityTimeDialog(false) },
                title = { Text("Время занятия") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = selectedSportActivityTime.split(":")[0],
                            onValueChange = { hours ->
                                setSelectedSportActivityTime("$hours:${selectedSportActivityTime.split(":")[1]}")
                            },
                            label = { Text("Часы:") },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            ),
                            modifier = Modifier.width(60.dp).weight(0.5f)
                        )
                        TextField(
                            value = selectedSportActivityTime.split(":")[1],
                            onValueChange = { minutes ->
                                setSelectedSportActivityTime("${selectedSportActivityTime.split(":")[0]}:$minutes")
                            },
                            label = { Text("Минуты:") },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            ),
                            modifier = Modifier.width(60.dp).weight(0.5f)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val hours = selectedSportActivityTime.split(":")[0].toIntOrNull() ?: 0
                        val minutes = selectedSportActivityTime.split(":")[1].toIntOrNull() ?: 0
                        val timeInMinutes = hours * 60 + minutes
                        val (row, day) = selectedSportActivityCell!!
                        viewModel.setSportActivityCellValue(row, day, timeInMinutes)
                        setShowSportActivityTimeDialog(false)
                        setSelectedSportActivityTime("0:0")
                    }) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    Button(onClick = { setShowSportActivityTimeDialog(false) }) {
                        Text("Отмена")
                    }
                }
            )
        }
        NewTopAppBar(navController = navController)
    }
}

@Composable
fun TargetsSection(
    targets: List<String>,
    viewModel: SportActivityViewModel,
    showDialog: Boolean,
    onAddClick: () -> Unit,
    newTarget: String,
    onNewTargetChange: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onDialogDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF386FAE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Цели",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                )
                IconButton(onClick = onAddClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Добавить цель",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (targets.isEmpty()) {
                Text(
                    text = "Цели не заданы",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 18.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column {
                    targets.forEachIndexed { index, target ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. $target",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            )
                            IconButton(onClick = { onRemoveClick(target) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_plus),
                                    contentDescription = "Удалить цель",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp).rotate(45f)
                                )
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = onDialogDismiss,
                    title = { Text("Добавить цель") },
                    text = {
                        TextField(
                            value = newTarget,
                            onValueChange = onNewTargetChange,
                            label = { Text("Введите цель") },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.DarkGray
                            )
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newTarget.isNotBlank()) {
                                viewModel.addSportActivityTarget(newTarget)
                                onNewTargetChange("")
                                onDialogDismiss()
                            }
                        }) {
                            Text("Добавить")
                        }
                    },
                    dismissButton = {
                        Button(onClick = onDialogDismiss) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TasksSection(
    tasks: List<Task>,
    viewModel: SportActivityViewModel,
    showDialog: Boolean,
    onAddClick: () -> Unit,
    newTask: String,
    onNewTaskChange: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onDialogDismiss: () -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC7E1FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Задачи",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                )
                IconButton(onClick = onAddClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Добавить задачу",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (tasks.isEmpty()) {
                Text(
                    text = "Задачи не заданы",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 18.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column {
                    tasks.forEachIndexed { index, task ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { isChecked ->
                                        onTaskCheckedChange(task.text, isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkmarkColor = Color.White,
                                        checkedColor = Color.Blue,
                                        uncheckedColor = Color.White
                                    )
                                )
                                Text(
                                    text = task.text,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                )
                            }
                            IconButton(onClick = { onRemoveClick(task.text) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_plus),
                                    contentDescription = "Удалить задачу",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp).rotate(45f)
                                )
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = onDialogDismiss,
                    title = { Text("Добавить задачу") },
                    text = {
                        TextField(
                            value = newTask,
                            onValueChange = onNewTaskChange,
                            label = { Text("Введите задачу") },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.DarkGray
                            )
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newTask.isNotBlank()) {
                                viewModel.addSportActivityTask(newTask)
                                onNewTaskChange("")
                                onDialogDismiss()
                            }
                        }) {
                            Text("Добавить")
                        }
                    },
                    dismissButton = {
                        Button(onClick = onDialogDismiss) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarSection(
    daysOfWeek: List<String>,
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    cellData: Map<Pair<Int, Int>, Int>,
    viewModel: SportActivityViewModel,
    onCellClick: (Int, Int) -> Unit,
    periodDropdownExpanded: Boolean,
    onPeriodDropdownChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A90E2))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Месяц: $selectedPeriod",
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                )
                Button(
                    onClick = { onPeriodDropdownChange(true) },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "выбрать",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }
                DropdownMenu(
                    expanded = periodDropdownExpanded,
                    onDismissRequest = { onPeriodDropdownChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("не задан") },
                        onClick = {
                            onPeriodChange("не задан")
                            onPeriodDropdownChange(false)
                        }
                    )
                    val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                onPeriodChange(month)
                                onPeriodDropdownChange(false)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .padding(4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            for (rowIndex in 0 until 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEachIndexed { dayIndex, _ ->
                        val cellKey = Pair(rowIndex, dayIndex)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onCellClick(dayIndex, rowIndex)
                                }
                                .border(
                                    BorderStroke(2.dp, Color.White),
                                    CircleShape
                                )
                                .background(
                                    if (cellData[cellKey] ?: 0 > 0) Color(0xFFFFFFFF) else Color.Transparent
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                        }
                    }
                }
            }
        }
    }
}