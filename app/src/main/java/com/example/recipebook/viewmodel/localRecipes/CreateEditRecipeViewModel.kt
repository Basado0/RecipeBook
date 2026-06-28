package com.example.recipebook.viewmodel.localRecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeRepository
import com.example.recipebook.data.local.datastore.SettingsDataStore
import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Instruction
import com.example.recipebook.models.Step
import com.example.recipebook.models.UserRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEditUiState(
    // Основные поля рецепта
    val title: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val readyInMinutes: String = "",
    val servings: String = "4",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList(),

    // Состояние сохранения
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val recipeId: Int? = null,
    val error: String? = null,
    val successMessage: String? = null,

    // Диалог ингредиента
    val showIngredientDialog: Boolean = false,
    val newIngredientName: String = "",
    val newIngredientAmount: String = "",
    val newIngredientUnit: String = "",

    // Диалог инструкции
    val showInstructionDialog: Boolean = false,
    val newInstructionName: String = "",
    val newInstructionSteps: List<String> = listOf("")
)

@HiltViewModel
class CreateEditRecipeViewModel @Inject constructor(
    private val userRecipeRepository: UserRecipeRepository,
    private val settingsDataStore: SettingsDataStore
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateEditUiState())
    val uiState: StateFlow<CreateEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.first().let { settings ->
                _uiState.update {
                    it.copy(servings = settings.defaultServings.toString())
                }
            }
        }
    }

    fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            val recipe = userRecipeRepository.getUserRecipeById(recipeId)
            if (recipe != null) {
                _uiState.update {
                    it.copy(
                        title = recipe.title,
                        description = recipe.description ?: "",
                        imageUri = recipe.imageUri,
                        readyInMinutes = recipe.readyInMinutes?.toString() ?: "",
                        servings = recipe.servings.toString(),
                        ingredients = recipe.ingredients,
                        instructions = recipe.instructions,
                        isEditMode = true,
                        recipeId = recipe.id
                    )
                }
            }
        }
    }

    // Обновление полей рецепта

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun updateReadyInMinutes(minutes: String) {
        _uiState.update { it.copy(readyInMinutes = minutes) }
    }

    fun updateServings(servings: String) {
        _uiState.update { it.copy(servings = servings) }
    }

    // Управление ингредиентами
    fun addIngredient(name: String, amount: Double, unit: String) {
        val ingredient = Ingredient(name = name, amount = amount, unit = unit)
        _uiState.update {
            it.copy(ingredients = it.ingredients + ingredient)
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update {
            it.copy(ingredients = it.ingredients.toMutableList().also { list ->
                list.removeAt(index)
            })
        }
    }

    // Диалог ингредиента

    fun showIngredientDialog() {
        _uiState.update { it.copy(showIngredientDialog = true) }
    }

    fun hideIngredientDialog() {
        _uiState.update {
            it.copy(
                showIngredientDialog = false,
                newIngredientName = "",
                newIngredientAmount = "",
                newIngredientUnit = ""
            )
        }
    }

    fun updateNewIngredientName(name: String) {
        _uiState.update { it.copy(newIngredientName = name) }
    }

    fun updateNewIngredientAmount(amount: String) {
        _uiState.update { it.copy(newIngredientAmount = amount) }
    }

    fun updateNewIngredientUnit(unit: String) {
        _uiState.update { it.copy(newIngredientUnit = unit) }
    }

    fun addIngredientFromDialog() {
        val state = _uiState.value
        val amount = state.newIngredientAmount.toDoubleOrNull() ?: 0.0

        if (state.newIngredientName.isNotBlank() && amount > 0) {
            addIngredient(state.newIngredientName, amount, state.newIngredientUnit)
            hideIngredientDialog()
        }
    }

    // Управление инструкциями

    fun addInstruction(name: String?, steps: List<Step>) {
        val instruction = Instruction(name = name, steps = steps)
        _uiState.update {
            it.copy(instructions = it.instructions + instruction)
        }
    }

    fun removeInstruction(index: Int) {
        _uiState.update {
            it.copy(instructions = it.instructions.toMutableList().also { list ->
                list.removeAt(index)
            })
        }
    }

    // ====== Диалог инструкции ======

    fun showInstructionDialog() {
        _uiState.update { it.copy(showInstructionDialog = true) }
    }

    fun hideInstructionDialog() {
        _uiState.update {
            it.copy(
                showInstructionDialog = false,
                newInstructionName = "",
                newInstructionSteps = listOf("")
            )
        }
    }

    fun updateNewInstructionName(name: String) {
        _uiState.update { it.copy(newInstructionName = name) }
    }

    fun addNewStep() {
        _uiState.update {
            it.copy(newInstructionSteps = it.newInstructionSteps + "")
        }
    }

    fun updateNewStep(index: Int, value: String) {
        _uiState.update { state ->
            val mutableList = state.newInstructionSteps.toMutableList()
            if (index in mutableList.indices) {
                mutableList[index] = value
            }
            state.copy(newInstructionSteps = mutableList)
        }
    }

    fun removeNewStep(index: Int) {
        _uiState.update { state ->
            val mutableList = state.newInstructionSteps.toMutableList()
            if (mutableList.size > 1 && index in mutableList.indices) {
                mutableList.removeAt(index)
            }
            state.copy(newInstructionSteps = mutableList)
        }
    }

    fun addInstructionFromDialog() {
        val state = _uiState.value
        val steps = state.newInstructionSteps
            .filter { it.isNotBlank() }
            .mapIndexed { index, step ->
                Step(
                    number = index + 1,
                    step = step.trim()
                )
            }

        if (steps.isNotEmpty()) {
            addInstruction(
                state.newInstructionName.ifBlank { null },
                steps
            )
            hideInstructionDialog()
        }
    }

    //Сохранение
    fun saveRecipe() {
        val state = _uiState.value

        // Валидация
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val recipe = UserRecipe(
                    id = state.recipeId ?: 0,
                    title = state.title,
                    imageUri = state.imageUri,
                    description = state.description.ifBlank { null },
                    readyInMinutes = state.readyInMinutes.toIntOrNull(),
                    servings = state.servings.toIntOrNull() ?: 4,
                    ingredients = state.ingredients,
                    instructions = state.instructions,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                if (state.isEditMode) {
                    userRecipeRepository.updateUserRecipe(recipe)
                } else {
                    userRecipeRepository.saveUserRecipe(recipe)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Recipe saved!",
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save recipe: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

}