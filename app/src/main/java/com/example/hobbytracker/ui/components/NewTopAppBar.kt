package com.example.hobbytracker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import com.example.hobbytracker.R
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.viewmodels.ProfileViewModel
import com.example.hobbytracker.util.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTopAppBar(
    navController: NavController,
    leftPadding: Dp = 10.dp,
    rightPadding: Dp = 10.dp
) {
    var isDrawerOpen by remember { mutableStateOf(false) }

    val drawerWidth = (LocalConfiguration.current.screenWidthDp * 0.75f).dp
    val drawerHeight = (LocalConfiguration.current.screenHeightDp * 0.75f).dp
    val drawerOffset by animateDpAsState(
        targetValue = if (isDrawerOpen) 0.dp else -drawerWidth,
        label = "drawerAnimation"
    )
    val closeDrawer = { isDrawerOpen = false }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { closeDrawer() }
                    .zIndex(1f)
            )
        }

        TopAppBar(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .zIndex(if (isDrawerOpen) 2f else 3f),
            title = { },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ColorUtils.TopBarColor(),
                titleContentColor = Color.Gray
            ),
            navigationIcon = {
                IconButton(
                    onClick = { isDrawerOpen = !isDrawerOpen },
                    modifier = Modifier
                        .size(40.dp)
                        .padding(start = leftPadding)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "Меню",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { }, //TODO поиск
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = rightPadding)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                }
            }
        )

        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .height(drawerHeight)
                    .width(drawerWidth)
                    .offset(x = drawerOffset)
                    .background(ColorUtils.translucentColor())
                    .zIndex(3f)
            ) {
                DrawerContent(navController, onClose = closeDrawer)
            }
        }
    }
}

@Composable
private fun DrawerContent(
    navController: NavController,
    onClose: () -> Unit
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profileState by profileViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile(context)
    }

    Column(modifier = Modifier
        .fillMaxHeight()) {
        Spacer(modifier = Modifier.height(64.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    profileState.profileImage != null -> {
                        Image(
                            bitmap = profileState.profileImage!!.asImageBitmap(),
                            contentDescription = "Аватар",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    profileState.photoUrl != null -> {
                        AsyncImageLoader(url = profileState.photoUrl!!)
                    }

                    profileState.firstName.isNotEmpty() || profileState.lastName.isNotEmpty() -> {
                        val initials = ColorUtils.getInitials(
                            profileState.firstName,
                            profileState.lastName
                        )
                        InitialsAvatar(
                            initials = initials,
                            size = 64.dp,
                            fontSize = 20.sp
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Аватар",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp)) {
                val name = buildString {
                    if (profileState.lastName.isNotEmpty()) append(profileState.lastName)
                    if (profileState.firstName.isNotEmpty()) {
                        if (isNotEmpty()) append(" ")
                        append(profileState.firstName)
                    }
                    if (isEmpty()) append("Гость")
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1
                )

                if (profileState.email.isNotEmpty()) {
                    Text(
                        text = profileState.email,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.7f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        val menuItems = listOf(
            MenuItem(
                title = "Профиль пользователя",
                iconPainter = painterResource(id = R.drawable.ic_profile),
                route = Screen.Profile.route
            ),
            MenuItem(
                title = "Моя активность",
                iconPainter = painterResource(id = R.drawable.ic_activity),
                route = Screen.Activity.route
            ),
            MenuItem(
                title = "Мои хобби",
                iconPainter = painterResource(id = R.drawable.ic_logo),
                preserveOriginalColor = true,
                route = Screen.Main.route
            ),
            MenuItem(
                title = "Выбор хобби",
                iconPainter = painterResource(id = R.drawable.ic_hobby),
                route = Screen.Main.route
            ),
            MenuItem(
                title = "Настройки",
                iconPainter = painterResource(id = R.drawable.ic_settings),
                route = Screen.Settings.route
            )
        )

        menuItems.forEach { item ->
            Column {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.7f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(item.route)
                            onClose()
                        }
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        item.iconPainter != null -> Icon(
                            painter = item.iconPainter,
                            contentDescription = item.title,
                            tint = if (item.preserveOriginalColor) Color.Unspecified else Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    Text(
                        item.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.7f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private data class MenuItem(
    val title: String,
    val iconPainter: Painter? = null,
    val preserveOriginalColor: Boolean = false,
    val route: String
)

@Composable
fun AsyncImageLoader(url: String) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .crossfade(true)
        .build()

    AsyncImage(
        model = url,
        contentDescription = null,
        imageLoader = imageLoader,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}