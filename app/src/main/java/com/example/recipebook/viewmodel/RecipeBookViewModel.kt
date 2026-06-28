package com.example.recipebook.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.api.MealRepository
import com.example.recipebook.data.local.datastore.SettingsDataStore
import com.example.recipebook.data.local.favourite.FavouriteRepository
import com.example.recipebook.data.local.history.HistoryRepository
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import com.example.recipebook.models.toMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
    private val historyRepository: HistoryRepository,
    private val settingsDataStore: SettingsDataStore
): ViewModel() {

    // Источник 1 - Поисковый запрос из UI
    private val _query = MutableStateFlow("")

    // Источник 2 - Действие "обновить" (retry)
    private val _retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Источник 3 - Выбранный ID рецепта
    private val _selectedMealId = MutableStateFlow<Int?>(null)

    //SharedFlow для retry рецепта
    private val _recipeRetryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Источник 4 - Избранное из Room (Flow из data layer)
    private val favouritesFlow: Flow<List<Meal>> = favouriteRepository.observeFavourites()

    // Источник 5 - Кэшированные результаты из Room (Flow из data layer)
    private val cachedMealsFlow: Flow<List<Meal>> = mealRepository.observeCachedMeals()

    // Источник 6 - История из Room (Flow из data layer)
    private val historyFlow: Flow<List<Meal>> = historyRepository.observeHistory()

    //ПРЕОБРАЗОВАНИЕ ПОИСКОВОГО ЗАПРОСА
    //debounce + distinctUntilChanged + trim
    @OptIn(FlowPreview::class)
    private val debouncedQuery: Flow<String> = _query
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .map { it.trim() }

    //СЕТЕВОЙ ПОИСК (реактивный)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchResultsFlow: Flow<List<Meal>> = debouncedQuery
        .flatMapLatest { query ->
            combine(
                flowOf(query),
                _retryTrigger.onStart { emit(Unit) }
            ) { currentQuery, _ -> currentQuery }
        }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                cachedMealsFlow
            } else {
                flow {
                    val settings = settingsDataStore.settingsFlow.first()
                    if (settings.offlineModeEnabled) {
                        val cached = mealRepository.getLastCachedMeals()
                        emit(cached)
                    } else {
                        emit(mealRepository.searchMeals(query))
                    }
                }.catch { e ->
                    emit(emptyList())
                }
            }
        }

    // СОСТОЯНИЕ ЗАГРУЗКИ И ОШИБОК ПОИСКА
    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchStateFlow: Flow<SearchState> = debouncedQuery
        .flatMapLatest { query ->
            combine(
                flowOf(query),
                _retryTrigger.onStart { emit(Unit) }
            ) { currentQuery, _ -> currentQuery }
        }
        .flatMapLatest { query ->
            flow {
                if (query.isBlank()) {
                    // Для кэша не показываем загрузку и ошибки
                    emit(SearchState(isLoading = false, error = null))
                } else {
                    emit(SearchState(isLoading = true, error = null))
                    try {
                        mealRepository.searchMeals(query)
                        emit(SearchState(isLoading = false, error = null))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        emit(SearchState(
                            isLoading = false,
                            error = "Search error: ${e.message}"
                        ))
                    }
                }
            }.catch { e ->
                emit(SearchState(isLoading = false, error = "Unexpected: ${e.message}"))
            }
        }

    // ЗАГРУЗКА РЕЦЕПТА
    @OptIn(ExperimentalCoroutinesApi::class)
    private val recipeFlow: Flow<RecipeState> = _selectedMealId
        .filterNotNull()
        .flatMapLatest { mealId ->
            combine(
                flowOf(mealId),
                _recipeRetryTrigger.onStart { emit(Unit) }
            ) { id, _ -> id }
        }
        .flatMapLatest { mealId ->
            flow {
                val settings = settingsDataStore.settingsFlow.first()
                if (settings.offlineModeEnabled) {
                    emit(RecipeState(error = "Offline mode enabled. Cannot load recipe details."))
                    return@flow
                }

                emit(RecipeState(isLoading = true))
                try {
                    val recipe = withTimeout(30_000) {
                        mealRepository.getRecipe(mealId)
                    }
                    viewModelScope.launch {
                        try {
                            if (settings.autoSaveHistory) {
                                historyRepository.addToHistory(recipe.toMeal())
                            }
                        } catch (_: Exception) { }
                    }
                    emit(RecipeState(recipe = recipe, isLoading = false))
                } catch (e: TimeoutCancellationException) {
                    emit(RecipeState(isLoading = false, error = "Request timed out. Please try again."))
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        emit(RecipeState(isLoading = false))
                    } else {
                        emit(RecipeState(isLoading = false, error = "Couldn't load recipe: ${e.message}"))
                    }
                }
            }
        }
        .onStart { emit(RecipeState()) }

    //Сначала объединяем часть потоков

    private val dataFlow: Flow<DataParams> = combine(
        _query,
        searchResultsFlow,
        cachedMealsFlow,
        historyFlow,
        favouritesFlow
    ) { query, searchResults, cached, history, favourites ->
        DataParams(query, searchResults, cached, history, favourites)
    }

    // Затем добавляем оставшиеся
    val uiState: StateFlow<RecipeBookUiState> = combine(
        dataFlow,
        searchStateFlow,
        recipeFlow
    ) { data, searchState, recipeState ->
        RecipeBookUiState(
            query = data.query,
            searchResults = if (data.query.isBlank()) data.cached else data.searchResults,
            history = data.history,
            isCacheLoading = false,
            isSearchLoading = searchState.isLoading,
            isHistoryLoading = false,
            searchError = searchState.error,
            cacheError = null,
            historyError = null,
            selectedRecipe = recipeState.recipe,
            isRecipeLoading = recipeState.isLoading,
            recipeError = recipeState.error,
            favourites = data.favourites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeBookUiState()
    )

    // ПУБЛИЧНЫЕ МЕТОДЫ ДЛЯ UI
    fun updateSearchQuery(query: String) {
        _query.value = query
    }

    fun retrySearch() {
        viewModelScope.launch {
            _retryTrigger.emit(Unit)
        }
    }

    fun loadRecipe(mealId: Int) {
        _selectedMealId.value = mealId
    }

    fun retryLoadRecipe() {
        viewModelScope.launch {
            _recipeRetryTrigger.emit(Unit)
        }
    }

    fun clearSelection() {
        _selectedMealId.value = null
    }

    fun toggleFavourite(mealId: Int) {
        viewModelScope.launch {
            // Берём актуальное состояние избранного из Flow
            val favourites = favouritesFlow.first()
            val isCurrentlyFavourite = favourites.any { it.id == mealId }

            if (isCurrentlyFavourite) {
                favouriteRepository.removeFromFavourites(mealId)
            } else {
                val meal = findMealById(mealId) ?: return@launch
                favouriteRepository.addToFavourites(meal)
            }
        }
    }

    // Поиск Meal по id в текущих данных
    private fun findMealById(mealId: Int): Meal? {
        val state = uiState.value
        return state.searchResults.find { it.id == mealId }
            ?: state.selectedRecipe?.let { Meal(id = it.id, title = it.title, image = it.image) }
            ?: state.history.find { it.id == mealId }
    }

    private data class SearchState(
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private data class RecipeState(
        val recipe: Recipe? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private data class DataParams(
        val query: String,
        val searchResults: List<Meal>,
        val cached: List<Meal>,
        val history: List<Meal>,
        val favourites: List<Meal>
    )
}

