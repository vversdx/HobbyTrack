package com.example.hobbytracker.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbytracker.R
import com.example.hobbytracker.ui.components.InitialsAvatar
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme.isDarkTheme
import com.example.hobbytracker.util.ColorUtils
import com.example.hobbytracker.util.ColorUtils.getInitials
import com.example.hobbytracker.viewmodels.ArtCraftViewModel
import com.example.hobbytracker.viewmodels.GamePlayViewModel
import com.example.hobbytracker.viewmodels.MusicHobbyViewModel
import com.example.hobbytracker.viewmodels.ProfileViewModel
import com.example.hobbytracker.viewmodels.ReadingBookViewModel
import com.example.hobbytracker.viewmodels.SportActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var editingState by remember { mutableStateOf(viewModel.state.value.copy()) }
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val originalState by viewModel.state.collectAsState()
    val musicVM: MusicHobbyViewModel = viewModel()
    val musicTime by musicVM.musicHobbyTotalTime.collectAsState()
    val sportVM: SportActivityViewModel = viewModel()
    val sportTime by sportVM.sportActivityTotalDuration.collectAsState()
    val gamesVM: GamePlayViewModel = viewModel()
    val gamesTime by gamesVM.gamePlayTotalDuration.collectAsState()
    val readingVM: ReadingBookViewModel = viewModel()
    val readingTime by readingVM.readingBookTotalDuration.collectAsState()
    val artVM: ArtCraftViewModel = viewModel()
    val artTime by artVM.artCraftTotalDuration.collectAsState()

    val activityData = remember {
        mapOf(
            "Спорт" to sportTime,
            "Искусство" to artTime,
            "Музыка" to musicTime,
            "Чтение" to readingTime,
            "Игры" to gamesTime,
        )
    }
    val totalActivity = activityData.values.sum().toFloat()
    val colors = listOf(Color.Red, Color.Yellow, Color.Magenta, Color.Blue, Color(0xFF8A2BE2))

    LaunchedEffect(originalState) {
        editingState = originalState.copy()
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            viewModel.handleImageSelection(context, selectedUri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фон
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((LocalConfiguration.current.screenHeightDp * 0.66f).dp)
                    .background(ColorUtils.MainBlue())
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(if (isDarkTheme) ColorUtils.MainBlueDark else Color.White)
            )
        }

        // Блок информации
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .padding(top = 220.dp, bottom = 24.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RectangleShape,
                    clip = false
                )
                .background(if (isDarkTheme) ColorUtils.GrayProfile else Color.White, RectangleShape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                ProfileField(
                    value = editingState.lastName,
                    onValueChange = { editingState = editingState.copy(lastName = it) },
                    label = "Фамилия",
                    isEditing = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileField(
                    value = editingState.firstName,
                    onValueChange = { editingState = editingState.copy(firstName = it) },
                    label = "Имя",
                    iconResId = R.drawable.ic_person,
                    iconSize = 35.dp,
                    isEditing = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileField(
                    value = editingState.middleName ?: "",
                    onValueChange = { editingState = editingState.copy(middleName = it) },
                    label = "Отчество",
                    isEditing = isEditing
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileField(
                    value = state.email,
                    onValueChange = {},
                    label = "Email",
                    iconResId = R.drawable.ic_email,
                    isEditable = false,
                    isEditing = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileField(
                    value = editingState.phone ?: "",
                    onValueChange = { editingState = editingState.copy(phone = it) },
                    label = "Телефон",
                    iconResId = R.drawable.ic_phone,
                    isEditing = isEditing,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(120.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                if (isEditing) {
                    IconButton(
                        onClick = {
                            isEditing = false
                            viewModel.updateProfile(
                                firstName = editingState.firstName,
                                lastName = editingState.lastName,
                                middleName = editingState.middleName,
                                phone = editingState.phone
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = ColorUtils.GraySecondary
                        )
                    }
                } else {
                    IconButton(
                        onClick = { isEditing = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_profile),
                            contentDescription = "Редактировать",
                            tint = ColorUtils.GraySecondary
                        )
                    }
                }
            }
        }

        // Аватарка
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp),
            contentAlignment = Alignment.TopCenter
        ) {

            Canvas(
                modifier = Modifier.size(160.dp)
            ) {
                var startAngle = -90f
                activityData.values.forEachIndexed { index, value ->
                    val sweepAngle = (value / totalActivity) * 360f
                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        size = size,
                        style = Stroke(width = 20f)
                    )
                    startAngle += sweepAngle
                }
            }

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        clip = true
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    state.profileImage?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Аватар",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        val initials = getInitials(state.firstName, state.lastName)
                        if (initials.isNotEmpty()) {
                            InitialsAvatar(
                                initials = initials,
                                size = 120.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Аватар",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 65.dp, y = 240.dp)
        ) {
            IconButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Gray
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_photo),
                    contentDescription = "Сменить фото",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        CenterAlignedTopAppBar(
            title = {},
            navigationIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = if (isDarkTheme) ColorUtils.GraySecondary else Color.Black
                        )
                    }
                    Text(
                        text = "Профиль",
                        color = ColorUtils.PrimaryColor(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}