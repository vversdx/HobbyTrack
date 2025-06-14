package com.example.hobbytracker.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import com.example.hobbytracker.ui.components.InitialsAvatar
import com.example.hobbytracker.util.ColorUtils.getInitials
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.util.ColorUtils
import com.example.hobbytracker.R
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.ui.theme.HobbyTrackerTheme.isDarkTheme
import com.example.hobbytracker.viewmodels.AuthViewModel
import com.example.hobbytracker.viewmodels.ProfileViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
                    .background(if (isDarkTheme) ColorUtils.GraySecondary else Color.White)
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
            Box(
                modifier = Modifier
                    .size(140.dp)
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
                .offset(x = 50.dp, y = 220.dp)
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