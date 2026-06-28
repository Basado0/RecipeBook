package com.example.recipebook.IntegrationTests.UI

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.recipebook.MainActivity
import com.example.recipebook.api.MealRepository
import com.example.recipebook.data.local.favourite.FavouriteRepository
import com.example.recipebook.data.local.history.HistoryRepository
import com.example.recipebook.models.Meal
import com.example.recipebook.viewmodel.SEARCH_DEBOUNCE_MS
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SearchScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockMealRepository: MealRepository = mockk(relaxed = true)

    @BindValue
    val mockFavouriteRepository: FavouriteRepository = mockk(relaxed = true)

    @BindValue
    val mockHistoryRepository: HistoryRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        hiltRule.inject()
        coEvery { mockFavouriteRepository.observeFavourites() } returns flowOf(emptyList())
        coEvery { mockHistoryRepository.getHistory() } returns emptyList()
        coEvery { mockMealRepository.getLastCachedMeals() } returns emptyList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun search_displaysResults_success() = runTest {
        val expectedMeals = listOf(
            Meal(1, "Pasta Carbonara", "img1"),
            Meal(2, "Pasta Primavera", "img2")
        )
        coEvery { mockMealRepository.searchMeals("pasta") } returns expectedMeals

        composeTestRule.onNodeWithTag("search_field").performTextInput("pasta")
        composeTestRule.waitForIdle()

        advanceTimeBy(SEARCH_DEBOUNCE_MS + 50)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("loading_indicator").assertDoesNotExist()

        composeTestRule.waitUntil(3000) {
            composeTestRule.onAllNodesWithTag("search_result_item_1")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("search_result_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_result_item_1").assertIsDisplayed()


        composeTestRule.onNodeWithText("No recipes found").assertDoesNotExist()
        composeTestRule.onNodeWithText("Error in the search").assertDoesNotExist()
    }
}
