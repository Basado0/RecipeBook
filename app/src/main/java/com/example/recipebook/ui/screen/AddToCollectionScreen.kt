package com.example.recipebook.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipebook.viewmodel.RecipeBookViewModel
import com.example.recipebook.viewmodel.localRecipes.CollectionsViewModel
import com.example.recipebook.viewmodel.localRecipes.MyRecipesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionScreen(
    collectionId: Int,
    onBackClick: () -> Unit,
    collectionsViewModel: CollectionsViewModel = hiltViewModel(),
    myRecipesViewModel: MyRecipesViewModel = hiltViewModel()
) {
    val myRecipesState by myRecipesViewModel.uiState.collectAsStateWithLifecycle()
    val collectionsState by collectionsViewModel.uiState.collectAsStateWithLifecycle()

    val collection = collectionsState.collections.find { it.id == collectionId }
    val existingRecipeIds = collection?.recipes?.map { it.recipeId } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add to Collection") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (myRecipesState.recipes.isNotEmpty()) {
                item {
                    Text(
                        "My Recipes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(myRecipesState.recipes) { recipe ->
                    val isAlreadyAdded = existingRecipeIds.contains(recipe.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAlreadyAdded) {
                                collectionsViewModel.addRecipeToCollection(
                                    collectionId = collectionId,
                                    recipeId = recipe.id
                                )
                                onBackClick()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isAlreadyAdded) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isAlreadyAdded)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                recipe.title,
                                modifier = Modifier.weight(1f),
                                color = if (isAlreadyAdded)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            if (isAlreadyAdded) {
                                Text(
                                    "Already added",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recipes available to add")
                    }
                }
            }
        }
    }
}