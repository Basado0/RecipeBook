package com.example.recipebook.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipebook.ui.dialog.AddIngredientDialog
import com.example.recipebook.ui.dialog.AddInstructionDialog
import com.example.recipebook.viewmodel.localRecipes.CreateEditRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditRecipeScreen(
    recipeId: Int? = null,
    onBackClick: () -> Unit,
    viewModel: CreateEditRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Загрузка рецепта при редактировании
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipe(recipeId)
        }
    }

    // Обработка успешного сохранения
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit Recipe" else "New Recipe")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveRecipe() },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ошибка
                if (uiState.error != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                uiState.error!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Основные поля
                item {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Recipe Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.readyInMinutes,
                            onValueChange = { viewModel.updateReadyInMinutes(it) },
                            label = { Text("Time (min)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.servings,
                            onValueChange = { viewModel.updateServings(it) },
                            label = { Text("Servings") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Ингредиенты
                item {
                    Text(
                        "Ingredients",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                itemsIndexed(uiState.ingredients) { index, ingredient ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${ingredient.amount} ${ingredient.unit} ${ingredient.name}",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeIngredient(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }

                // Инструкции
                item {
                    Text(
                        "Instructions",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                itemsIndexed(uiState.instructions) { index, instruction ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (!instruction.name.isNullOrBlank()) {
                                Text(
                                    instruction.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            instruction.steps.forEach { step ->
                                Text("${step.number}. ${step.step}")
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showIngredientDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ingredient")
                        }

                        Button(
                            onClick = { viewModel.showInstructionDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Instruction")
                        }
                    }
                }
            }
        }
    }

    if (uiState.showIngredientDialog) {
        AddIngredientDialog(
            name = uiState.newIngredientName,
            amount = uiState.newIngredientAmount,
            unit = uiState.newIngredientUnit,
            onNameChange = viewModel::updateNewIngredientName,
            onAmountChange = viewModel::updateNewIngredientAmount,
            onUnitChange = viewModel::updateNewIngredientUnit,
            onDismiss = viewModel::hideIngredientDialog,
            onAdd = viewModel::addIngredientFromDialog
        )
    }

    if (uiState.showInstructionDialog) {
        AddInstructionDialog(
            instructionName = uiState.newInstructionName,
            steps = uiState.newInstructionSteps,
            onNameChange = viewModel::updateNewInstructionName,
            onStepChange = viewModel::updateNewStep,
            onAddStep = viewModel::addNewStep,
            onRemoveStep = viewModel::removeNewStep,
            onDismiss = viewModel::hideInstructionDialog,
            onAdd = viewModel::addInstructionFromDialog
        )
    }
}