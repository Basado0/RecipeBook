package com.example.recipebook.IntegrationTests.UI

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipebook.models.Meal
import com.example.recipebook.ui.screen.FavouritesScreen
import com.example.recipebook.ui.screen.SearchScreen
import com.example.recipebook.viewmodel.RecipeBookUiState
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun selectingMealFromSearch_navigatesToDetailScreen() {
        val meals = listOf(Meal(id = 1, title = "Test Pasta", image = ""))
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        uiState = RecipeBookUiState(
                            searchResults = meals,
                            isCacheLoading = false
                        ),
                        onSearchChange = {},
                        onMealClick = { meal ->
                            navController.navigate("detail/${meal.id}")
                        },
                        onNavigateToFavourites = {},
                        onNavigateToHistory = {},
                        onToggleFavourite = {}
                    )
                }
                composable(
                    route = "detail/{mealId}",
                    arguments = listOf(navArgument("mealId") { type = NavType.IntType })
                ) { entry ->
                    val mealId = entry.arguments?.getInt("mealId")
                    Text("Detail screen for meal $mealId")
                }
            }
        }

        composeTestRule.onNodeWithText("Test Pasta").performClick()
        composeTestRule.waitForIdle()

        // Прямая проверка аргумента навигации
        val actualMealId = navController.currentBackStackEntry?.arguments?.getInt("mealId")
        assertEquals(1, actualMealId)

        composeTestRule.onNodeWithText("Detail screen for meal 1").assertIsDisplayed()
    }

    @Test
    fun clickingFavouritesButton_navigatesToFavouritesScreen() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        uiState = RecipeBookUiState(isCacheLoading = false),
                        onSearchChange = {},
                        onMealClick = {},
                        onNavigateToFavourites = { navController.navigate("favourites") },
                        onNavigateToHistory = {},
                        onToggleFavourite = {}
                    )
                }
                composable("favourites") {
                    Text("Favourites Screen")
                }
            }
        }


        composeTestRule.onNodeWithContentDescription("Favourites").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Favourites Screen").assertIsDisplayed()
        assertEquals("favourites", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun clickingHistoryButton_navigatesToHistoryScreen() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        uiState = RecipeBookUiState(isCacheLoading = false),
                        onSearchChange = {},
                        onMealClick = {},
                        onNavigateToFavourites = {},
                        onNavigateToHistory = { navController.navigate("history") },
                        onToggleFavourite = {}
                    )
                }
                composable("history") {
                    Text("History Screen")
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("History").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("History Screen").assertIsDisplayed()
        assertEquals("history", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun pressingBackFromFavourites_returnsToSearch() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        uiState = RecipeBookUiState(isCacheLoading = false),
                        onSearchChange = {},
                        onMealClick = {},
                        onNavigateToFavourites = { navController.navigate("favourites") },
                        onNavigateToHistory = {},
                        onToggleFavourite = {}
                    )
                }
                composable("favourites") {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                    Text("Favourites Screen")
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Favourites").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Favourites Screen").assertIsDisplayed()

        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Favourites Screen").assertDoesNotExist()

        assertEquals("search", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun selectingMealFromFavourites_navigatesToDetailScreen() {
        val favouriteMeals = listOf(Meal(id = 42, title = "Favourite Pizza", image = ""))

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "favourites") {
                composable("favourites") {
                    FavouritesScreen(
                        uiState = RecipeBookUiState(favourites = favouriteMeals, isCacheLoading = false),
                        onMealClick = { meal ->
                            navController.navigate("detail/${meal.id}")
                        },
                        onToggleFavourite = {},
                        onNavigateBack = {}
                    )
                }
                composable("detail/{mealId}") { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getString("mealId")
                    Text("Detail screen for meal $mealId")
                }
            }
        }

        composeTestRule.onNodeWithText("Favourite Pizza").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Detail screen for meal 42").assertIsDisplayed()
    }

}