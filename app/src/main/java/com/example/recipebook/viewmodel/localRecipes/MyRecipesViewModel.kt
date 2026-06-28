package com.example.recipebook.viewmodel.localRecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeRepository
import com.example.recipebook.models.UserRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State для экрана "Мои рецепты"
data class MyRecipesUiState(
    val recipes: List<UserRecipe> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userRecipeRepository: UserRecipeRepository
): ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val userRecipesFlow: Flow<List<UserRecipe>> =
        userRecipeRepository.observeAllUserRecipes()

    // Итоговое состояние
    val uiState: StateFlow<MyRecipesUiState> = combine(
        userRecipesFlow,
        _isLoading,
        _error
    ) { recipes, isLoading, error ->
        MyRecipesUiState(
            recipes = recipes,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyRecipesUiState()
    )

    init {
        // Сбрасываем загрузку после получения данных
        viewModelScope.launch {
            userRecipesFlow.collect {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                userRecipeRepository.deleteUserRecipe(recipeId)
            } catch (e: Exception) {
                _error.value = "Failed to delete recipe"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}