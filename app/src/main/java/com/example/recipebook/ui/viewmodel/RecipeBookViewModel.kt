package com.example.recipebook.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.api.MealRepository
import com.example.recipebook.api.MealSearchResult
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class RecipeBookUiState(
    val query: String = "",
    val searchResults: List<Meal> = emptyList(),
    val isSearchLoading: Boolean = false,
    val searchError: String? = null,

    val selectedRecipe: Recipe? = null,
    val isRecipeLoading: Boolean = false,
    val recipeError: String? = null,

    val favourites: List<Meal> = emptyList()
)

class RecipeBookViewModel(private val repository: MealRepository = MealRepository()): ViewModel() {
    var uiState by mutableStateOf(RecipeBookUiState())
        private set

    private var searchJob: Job? = null
    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(
            query = query
        )
        searchJob?.cancel()
        searchJob = viewModelScope.launch{
            delay(500)
            searchMeals()
        }
    }

    fun toggleFavourite(mealId: Int) {
        val isCurrentlyFavourite = uiState.favourites.any { it.id == mealId }
        if (isCurrentlyFavourite) {
            val newFavourites = uiState.favourites.filter { it.id != mealId }
            uiState = uiState.copy(
                favourites = newFavourites
            )
        } else {
            val mealToAdd = uiState.searchResults.find { it.id == mealId }
            if (mealToAdd != null) {
                val newFavourites = uiState.favourites + mealToAdd
                uiState = uiState.copy(
                    favourites = newFavourites
                )
            }
        }
    }

        fun searchMeals() {
            if (uiState.query.isBlank()) {
                uiState = uiState.copy(searchError = "Enter the search query")
                return
            }
            uiState = uiState.copy(
                isSearchLoading = true,
                searchError = null,
                searchResults = emptyList()
            )

            viewModelScope.launch {
                try {
                    val results = repository.searchMeals(uiState.query)
                    uiState = uiState.copy(
                        isSearchLoading = false,
                        searchResults = results
                    )
                } catch (ex: Exception) {
                    uiState = uiState.copy(
                        isSearchLoading = false,
                        searchError = "Error in the search: ${ex.message}"
                    )
                }
            }
        }

        fun loadRecipe(mealId: Int) {
            uiState = uiState.copy(
                selectedRecipe = null,
                isRecipeLoading = true,
                recipeError = null
            )

            viewModelScope.launch {
                try {
                    val recipe = repository.getRecipe(mealId)
                    uiState = uiState.copy(
                        selectedRecipe = recipe,
                        isRecipeLoading = false
                    )
                } catch (ex: Exception) {
                    uiState = uiState.copy(
                        isRecipeLoading = false,
                        recipeError = "Couldn't upload recipe"
                    )
                }
            }
        }

        fun clearSelection() {
            uiState = uiState.copy(
                selectedRecipe = null,
                recipeError = null
            )
        }
}

