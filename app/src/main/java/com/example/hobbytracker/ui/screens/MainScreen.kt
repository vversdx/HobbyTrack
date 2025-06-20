package com.example.hobbytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.hobbytracker.R
import com.example.hobbytracker.data.HobbyCategory
import com.example.hobbytracker.navigation.Screen
import com.example.hobbytracker.ui.components.NewTopAppBar
import com.example.hobbytracker.viewmodels.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbytracker.util.ColorUtils
import android.util.Log

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.zIndex(0f),
            topBar = { Box(modifier = Modifier.height(48.dp)) },
            floatingActionButton = {}
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                item {
                    Text(
                        text = "Hobby",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = ColorUtils.HobbyCategoryColor(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 60.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        textAlign = TextAlign.Center
                    )
                }
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        navController = navController,
                        onAddClick = {
                            // TODO Плюсики
                        }
                    )
                }
            }
        }

        NewTopAppBar(navController = navController)
    }
}

@Composable
fun CategoryItem(
    category: HobbyCategory,
    navController: NavController,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(80.dp)
            .clickable {
                Log.d("CategoryItem", "Clicked category: ${category.name}, id: ${category.id}")
                if (category.id == "1") {
                    navController.navigate(Screen.MusicHobby.route)
                }
                else if (category.id == "2"){
                    navController.navigate(Screen.SportHobby.route)
                }
                else if (category.id == "3"){
                    navController.navigate(Screen.ArtHobby.route)
                }
                else if (category.id == "4"){
                    navController.navigate(Screen.ReadingHobby.route)
                }
                else if (category.id == "5"){
                    navController.navigate(Screen.GamesHobby.route)
                }
                else if (category.id == "6"){
                    navController.navigate(Screen.OtherHobby.route)
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorUtils.HobbyCategoryColor()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = category.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .sizeIn(maxWidth = 44.dp, maxHeight = 44.dp)
                            .aspectRatio(1f),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = ColorUtils.CategoryName(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            // TODO Плюсики
            IconButton(
                onClick = { onAddClick() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Добавить хобби",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
    }
}