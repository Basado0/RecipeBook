package com.example.recipebook.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.recipebook.models.Meal
import com.example.recipebook.ui.viewmodel.RecipeBookUiState
import com.example.recipebook.ui.widget.MealCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun searchScreen(
    uiState: RecipeBookUiState,
    onSearchChange: (String) -> Unit,
    onMealClick: (Meal) -> Unit,
    onNavigateToFavourites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onToggleFavourite: (Int) -> Unit,

) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Search") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.Info, contentDescription = "History")
                    }
                    // Кнопка избранного в правом верхнем углу
                    IconButton(onClick = onNavigateToFavourites) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Favourites"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Закрепленная сверху поисковая строка с отступами
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Row {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = onSearchChange,
                        label = { Text("Name of the dish") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            when {
                uiState.isSearchLoading or uiState.isCacheLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.searchError != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                uiState.searchError,
                                color = Color.Red,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                    }

                }

                uiState.query.isBlank() && uiState.searchResults.isNotEmpty() -> {
                    Text("Last results:", modifier = Modifier.padding(16.dp))
                    LazyColumn {
                        items(uiState.searchResults) { meal ->
                            MealCard(
                                meal = meal,
                                isFavourite = meal in uiState.favourites,
                                onClick = { onMealClick(meal) }
                            ) { onToggleFavourite(meal.id) }
                        }
                    }
                }

                uiState.query.isBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Enter a recipe search query")
                    }
                }

                uiState.searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recipes found")
                    }

                }

                else -> {
                    LazyColumn {
                        items(uiState.searchResults) { meal ->
                            MealCard(
                                meal = meal,
                                isFavourite = meal in uiState.favourites,
                                onClick = { onMealClick(meal) }
                            ) { onToggleFavourite(meal.id) }
                        }
                    }
                }
            }
        }
    }
}


