package com.example.recipebook.viewmodel.localRecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeRepository
import com.example.recipebook.models.UserRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserRecipeDetailUiState(
    val recipe: UserRecipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserRecipeDetailViewModel @Inject constructor(
    private val userRecipeRepository: UserRecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserRecipeDetailUiState())
    val uiState: StateFlow<UserRecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val recipe = userRecipeRepository.getUserRecipeById(recipeId)
                _uiState.update {
                    it.copy(
                        recipe = recipe,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load recipe"
                    )
                }
            }
        }
    }
}