package com.example.recipebook.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipebook.ui.widget.CollectionRecipeCard
import com.example.recipebook.viewmodel.localRecipes.CollectionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Int,
    onRecipeClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onAddRecipeClick: (Int) -> Unit,
    viewModel: CollectionsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val collection = uiState.collections.find { it.id == collectionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection?.name ?: "Collection") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddRecipeClick(collectionId) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add recipe")
            }
        }
    ) { paddingValues ->
        when {
            collection == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            collection.recipes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("No recipes in this collection")
                        Button(onClick = { onAddRecipeClick(collectionId) }) {
                            Text("Add Recipe")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(collection.recipes) { recipe ->
                        CollectionRecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.recipeId) },
                            onRemove = {
                                viewModel.removeRecipeFromCollection(
                                    collectionId,
                                    recipe.recipeId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
