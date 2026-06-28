package com.example.recipebook.vm

import com.example.recipebook.api.MealRepository
import com.example.recipebook.data.local.favourite.FavouriteRepository
import com.example.recipebook.data.local.history.HistoryRepository
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import com.example.recipebook.models.toMeal
import com.example.recipebook.viewmodel.RecipeBookUiState
import com.example.recipebook.viewmodel.RecipeBookViewModel
import com.example.recipebook.viewmodel.SEARCH_DEBOUNCE_MS
import com.example.recipebook.utils.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.collections.emptyList


@ExperimentalCoroutinesApi
class RecipeBookViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mealRepository: MealRepository
    private lateinit var favouriteRepository: FavouriteRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var viewModel: RecipeBookViewModel

    @Before
    fun setup() {
        mealRepository = mockk()
        favouriteRepository = mockk()
        historyRepository = mockk()

        // Стабы для инициализации ViewModel
        coEvery { mealRepository.getLastCachedMeals() } returns emptyList()
        coEvery { historyRepository.getHistory() } returns emptyList()
        coEvery { favouriteRepository.observeFavourites() } returns flowOf(emptyList())

        viewModel = RecipeBookViewModel(
            mealRepository,
            favouriteRepository,
            historyRepository
        )
    }

    // 1. Корректное начальное состояние
    @Test
    fun `initial state is correct`(){
        val state = viewModel.uiState
        assertEquals(RecipeBookUiState(),state)
    }

    // 2. Успешная загрузка данных
    @Test
    fun `searchMeals success updates state`() = runTest {
        val query = "pasta"
        val expectedMeals = listOf(Meal(1,"Pasta","img1"),Meal(2,"Pasta2","img2"))
        coEvery { mealRepository.searchMeals(query) } returns expectedMeals

        viewModel.updateSearchQuery(query)
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        val state = viewModel.uiState
        assertFalse(state.isSearchLoading)
        assertEquals(expectedMeals,state.searchResults)
        assertNull(state.searchError)
    }

    // 3. Ошибка загрузки
    @Test
    fun `searchMeals error sets error state`() = runTest {
        val query = "pasta"
        val errorMessage = "Network error"
        coEvery { mealRepository.searchMeals(query) } throws IOException(errorMessage)

        viewModel.updateSearchQuery(query)
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        val state = viewModel.uiState
        assertFalse(state.isSearchLoading)
        assertTrue(state.searchResults.isEmpty())
        assertNotNull(state.searchError)
        assertTrue(state.searchError!!.contains(errorMessage))
    }

    // 4. Retry после ошибки
    @Test
    fun `retry after error performs search again and succeeds`() = runTest {
        val query = "pasta"
        val expectedMeals = listOf(Meal(1,"Pasta","img1"))
        coEvery { mealRepository.searchMeals(query) } throws IOException("error") andThen expectedMeals

        viewModel.updateSearchQuery(query)
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        assertNotNull(viewModel.uiState.searchError)

        viewModel.retrySearch()
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        val state = viewModel.uiState
        assertFalse(state.isSearchLoading)
        assertEquals(expectedMeals, state.searchResults)
        assertNull(state.searchError)
    }

    //5. Обработка пустого результата поиска
    @Test
    fun `searchMeals returns empty list clears results and no error`() = runTest {
        val query = "nonexistent"
        coEvery { mealRepository.searchMeals(query) } returns emptyList()

        viewModel.updateSearchQuery(query)
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        val state = viewModel.uiState
        assertFalse(state.isSearchLoading)
        assertTrue(state.searchResults.isEmpty())
        assertNull(state.searchError)
    }

    //6. Корректная загрузка истории
    @Test
    fun `loadHistory success updates state`() = runTest {
        val historyMeals = listOf(Meal(1, "Pasta", "img1"), Meal(2, "Pizza", "img2"))
        coEvery { historyRepository.getHistory() } returns historyMeals

        viewModel = RecipeBookViewModel(mealRepository, favouriteRepository, historyRepository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.isHistoryLoading)
        assertEquals(historyMeals, state.history)
        assertNull(state.historyError)
    }

    //7. Ошибка загрузки истории
    @Test
    fun `loadHistory error sets historyError`() = runTest {
        val error = IOException("Database error")
        coEvery { historyRepository.getHistory() } throws error

        viewModel = RecipeBookViewModel(mealRepository, favouriteRepository, historyRepository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.isHistoryLoading)
        assertNotNull(state.historyError)
        assertTrue(state.historyError!!.contains("Could not load history"))
    }

    //8. Загрузка последних результатов поиска при пустом запросе
    @Test
    fun `updateSearchQuery with blank query loads cached results`() = runTest {
        val cachedMeals = listOf(Meal(3, "Cached meal", "img3"))
        coEvery { mealRepository.getLastCachedMeals() } returns emptyList()

        viewModel = RecipeBookViewModel(mealRepository, favouriteRepository, historyRepository)
        advanceUntilIdle()

        assertEquals(emptyList<Meal>(), viewModel.uiState.searchResults)

        coEvery { mealRepository.getLastCachedMeals() } returns cachedMeals

        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        assertEquals(cachedMeals, viewModel.uiState.searchResults)
        assertFalse(viewModel.uiState.isCacheLoading)
        assertNull(viewModel.uiState.cacheError)
    }

    //9. Корректная работа loadRecipe и добавления в историю
    @Test
    fun `loadRecipe adds recipe to history and updates state`() = runTest {
        val mealId = 42
        val recipe = Recipe(
            id = mealId,
            title = "Risotto",
            image = "img42",
            readyInMinutes = 20,
            summary = "...",
            cuisines = emptyList(),
            instructions = emptyList(),
            ingredients = emptyList()
        )

        val expectedMeal = recipe.toMeal()
        val historyStore = mutableListOf<Meal>()

        coEvery { mealRepository.getRecipe(mealId) } returns recipe
        coEvery { historyRepository.addToHistory(expectedMeal) } answers {
            historyStore.add(expectedMeal)
        }
        coEvery { historyRepository.getHistory() } returns historyStore

        viewModel.loadRecipe(mealId)
        advanceUntilIdle()

        coVerify(exactly = 1) { mealRepository.getRecipe(mealId) }
        coVerify(exactly = 1) { historyRepository.addToHistory(expectedMeal) }
        coVerify(atLeast = 1) { historyRepository.getHistory() }

        val state = viewModel.uiState
        assertEquals(recipe, state.selectedRecipe)
        assertFalse(state.isRecipeLoading)
        assertNull(state.recipeError)
        assertEquals(listOf(expectedMeal),state.history)
    }

    //10. Отмена предыдущего поиска при новом вводе (debounce)
    @Test
    fun `new search cancels previous one`() = runTest {
        val query1 = "pas"
        val query2 = "pasta"
        val meal = Meal(1, "Pasta", "img1")

        coEvery { mealRepository.searchMeals(query1) } coAnswers {
            delay(SEARCH_DEBOUNCE_MS)
            listOf(meal)
        }
        coEvery { mealRepository.searchMeals(query2) } returns listOf(meal)

        viewModel.updateSearchQuery(query1)

        viewModel.updateSearchQuery(query2)

        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()

        advanceUntilIdle()

        coVerify(exactly = 1) { mealRepository.searchMeals(query2) }
        coVerify(exactly = 0) { mealRepository.searchMeals(query1) }
        assertEquals(listOf(meal), viewModel.uiState.searchResults)
    }

    //11.
    @Test
    fun `cache loading updates state correctly`() = runTest {
        val cachedMeals = listOf(Meal(1, "Pasta", "img1"))
        coEvery { mealRepository.getLastCachedMeals() } returns cachedMeals

        viewModel = RecipeBookViewModel(mealRepository, favouriteRepository, historyRepository)

        assertTrue(viewModel.uiState.isCacheLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.isCacheLoading)
        assertNull(viewModel.uiState.cacheError)
        assertEquals(cachedMeals, viewModel.uiState.searchResults)
    }
}