package com.example.recipebook.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.api.MealRepository
import com.example.recipebook.data.local.favourite.FavouriteRepository
import com.example.recipebook.data.local.history.HistoryRepository
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import com.example.recipebook.models.toMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val SEARCH_DEBOUNCE_MS = 500L
data class RecipeBookUiState(
    val query: String = "",
    val searchResults: List<Meal> = emptyList(),
    val history: List<Meal> = emptyList(),
    val isCacheLoading: Boolean = true,
    val isSearchLoading: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val searchError: String? = null,
    val cacheError: String? = null,
    val historyError: String? = null,

    val selectedRecipe: Recipe? = null,
    val isRecipeLoading: Boolean = false,
    val recipeError: String? = null,

    val favourites: List<Meal> = emptyList()
)

@HiltViewModel
class RecipeBookViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val favouriteRepository: FavouriteRepository,
    private val historyRepository: HistoryRepository
): ViewModel() {
    var uiState by mutableStateOf(RecipeBookUiState())
        private set

    private var searchJob: Job? = null

    init {
        observeFavourites()
        loadCachedResults()
        loadHistory()
    }

    private fun observeFavourites() {
        viewModelScope.launch {
            favouriteRepository.observeFavourites().collect { meals ->
                uiState = uiState.copy(favourites = meals)
            }
        }
    }

    private fun loadCachedResults() {
        viewModelScope.launch {
            try {
                val cached = mealRepository.getLastCachedMeals()
                uiState = uiState.copy(
                    searchResults = cached,
                    isCacheLoading = false,
                    cacheError = null
                )
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                uiState = uiState.copy(
                    isCacheLoading = false,
                    cacheError = "Could not load cached results"
                )
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            uiState = uiState.copy(isHistoryLoading = true)
            try {
                val history = historyRepository.getHistory()
                uiState = uiState.copy(
                    isHistoryLoading = false,
                    history = history,
                    historyError = null
                )
            } catch (ex: Exception) {
                if (ex is CancellationException) throw ex
                uiState = uiState.copy(
                    isHistoryLoading = false,
                    historyError = "Could not load history"
                )
            }
        }
    }
    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(
            query = query
        )
        searchJob?.cancel()
        if (query.isBlank()) {
            loadCachedResults() //теперь показываются последние результаты поиска при пустой строке
            return
        }
        searchJob = viewModelScope.launch{
            delay(SEARCH_DEBOUNCE_MS)
            searchMeals()
        }
    }
    private fun findMealById(mealId: Int): Meal? {
        uiState.searchResults.find { it.id == mealId}?.let { return it }
        val recipe = uiState.selectedRecipe
        if (recipe != null && recipe.id == mealId) {
            return Meal(id = recipe.id, title = recipe.title, image = recipe.image)
        }
        return null
    }
    fun toggleFavourite(mealId: Int) {
        viewModelScope.launch {
            val isCurrentlyFavourite = uiState.favourites.any { it.id == mealId }
            if (isCurrentlyFavourite) {
                favouriteRepository.removeFromFavourites(mealId)
            } else {
                    val meal = findMealById(mealId) ?: return@launch
                    favouriteRepository.addToFavourites(meal)
                }
            }
        }

        //Теперь suspend, запускается в job и сетевой запрос корректно отменяется
        private suspend fun searchMeals() {

            uiState = uiState.copy(
                isSearchLoading = true,
                searchError = null,
                searchResults = emptyList()
            )

            try {
                val results = mealRepository.searchMeals(uiState.query)
                uiState = uiState.copy(
                    isSearchLoading = false,
                    searchResults = results
                )
            } catch (ex: Exception) {
                if (ex is CancellationException) {
                    uiState = uiState.copy(isSearchLoading = false)
                    throw ex
                }
                uiState = uiState.copy(
                    isSearchLoading = false,
                    searchError = "Error in the search: ${ex.message}")
            }
        }

        fun retrySearch() {
            val currentQuery = uiState.query
            if (currentQuery.isNotBlank()) {
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(SEARCH_DEBOUNCE_MS)
                    searchMeals()
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
                    val recipe = mealRepository.getRecipe(mealId)
                    historyRepository.addToHistory(recipe.toMeal())
                    loadHistory()
                    uiState = uiState.copy(
                        selectedRecipe = recipe,
                        isRecipeLoading = false
                    )
                } catch (ex: Exception) {
                    if (ex is CancellationException) throw ex // обработка CancellationException
                    uiState = uiState.copy(
                        isRecipeLoading = false,
                        recipeError = "Couldn't load recipe"
                    )
                }
            }
        }
        fun clearSelection() {
            uiState = uiState.copy(
                recipeError = null
            )
        }
}

