package com.example.recipebook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipebook.ui.screen.FavouritesScreen
import com.example.recipebook.ui.screen.HistoryScreen
import com.example.recipebook.ui.screen.RecipeDetailScreen
import com.example.recipebook.ui.screen.SearchScreen
import com.example.recipebook.viewmodel.RecipeBookViewModel

sealed class Screen(val route: String){
    object Search: Screen("search")

    object Favourites: Screen("favourites")
    object Detail: Screen("detail/{mealId}"){
        fun createRoute(mealId:Int): String = "detail/$mealId"
    }

    object History: Screen("history")
}

@Composable
fun RecipeBookApp(){
    val holder: RecipeBookViewModel = hiltViewModel()
    val navController: NavHostController = rememberNavController()

    val uiState by holder.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ){
        composable(route = Screen.Search.route){
            SearchScreen(
                uiState = uiState,
                onSearchChange = holder::updateSearchQuery,
                onMealClick = { meal ->
                    navController.navigate(Screen.Detail.createRoute(meal.id))
                },
                onNavigateToFavourites = { navController.navigate(Screen.Favourites.route) },
                onToggleFavourite = holder::toggleFavourite,
                onNavigateToHistory = { navController.navigate(Screen.History.route)},
                onRetry = holder::retrySearch
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("mealId"){
                    type = NavType.IntType
                }
            )
        )
        { backStackEntry ->
            // Получаем ID из аргументов
            val mealId = backStackEntry.arguments?.getInt("mealId")

            if (mealId != null) {
                LaunchedEffect(mealId) {
                    holder.loadRecipe(mealId)
                }
            }
            RecipeDetailScreen(
               uiState = uiState,
                onBackClick = {
                    holder.clearSelection()
                    navController.popBackStack()
                },
                onToggleFavourite = {
                    mealId?.let { holder.toggleFavourite(it) }
                },
                onRetry = {
                    holder.retryLoadRecipe()
                }
            )
        }

        composable(
            route = Screen.Favourites.route
        ){
            FavouritesScreen(
                uiState = uiState,
                onMealClick = { meal ->
                    navController.navigate(Screen.Detail.createRoute(meal.id))
                },
                onToggleFavourite = holder::toggleFavourite,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.History.route
        ){
            HistoryScreen(
                uiState = uiState,
                onMealClick = { meal ->
                    navController.navigate(Screen.Detail.createRoute(meal.id))
                },
                onToggleFavourite = holder::toggleFavourite,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
