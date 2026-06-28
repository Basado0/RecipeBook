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
import com.example.recipebook.ui.screen.AddToCollectionScreen
import com.example.recipebook.ui.screen.CollectionDetailScreen
import com.example.recipebook.ui.screen.CollectionsScreen
import com.example.recipebook.ui.screen.CreateEditRecipeScreen
import com.example.recipebook.ui.screen.FavouritesScreen
import com.example.recipebook.ui.screen.HistoryScreen
import com.example.recipebook.ui.screen.MyRecipesScreen
import com.example.recipebook.ui.screen.RecipeDetailScreen
import com.example.recipebook.ui.screen.SearchScreen
import com.example.recipebook.ui.screen.SettingsScreen
import com.example.recipebook.ui.screen.UserRecipeDetailScreen
import com.example.recipebook.viewmodel.RecipeBookViewModel
import com.example.recipebook.viewmodel.localRecipes.CollectionsViewModel

sealed class Screen(val route: String){
    object Search: Screen("search")

    object Favourites: Screen("favourites")
    object Detail: Screen("detail/{mealId}"){
        fun createRoute(mealId:Int): String = "detail/$mealId"
    }

    object History: Screen("history")

    object MyRecipes : Screen("my_recipes")
    object CreateEditRecipe : Screen("create_edit/{recipeId}") {
        fun createRoute(recipeId: Int? = null) =
            "create_edit/${recipeId ?: -1}"
    }

    object Collections : Screen("collections")

    object CollectionDetail : Screen("collection/{collectionId}") {
        fun createRoute(collectionId: Int) = "collection/$collectionId"
    }

    object UserRecipeDetail : Screen("user_recipe/{recipeId}") {
        fun createRoute(recipeId: Int) = "user_recipe/$recipeId"
    }

    object AddToCollection : Screen("add_to_collection/{collectionId}") {
        fun createRoute(collectionId: Int) = "add_to_collection/$collectionId"
    }

    object Settings : Screen("settings")
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
                onRetry = holder::retrySearch,
                onNavigateToMyRecipes = { navController.navigate(Screen.MyRecipes.route) },
                onNavigateToCollections = { navController.navigate(Screen.Collections.route) },
                onNavigateToSettings =  {navController.navigate(Screen.Settings.route)}
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

        composable(route = Screen.MyRecipes.route) {
            MyRecipesScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.UserRecipeDetail.createRoute(recipeId))
                },
                onEditClick = { recipeId ->
                    navController.navigate(Screen.CreateEditRecipe.createRoute(recipeId))
                },
                onAddClick = {
                    navController.navigate(Screen.CreateEditRecipe.createRoute(null))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateEditRecipe.route,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId")?.takeIf { it != -1 }
            CreateEditRecipeScreen(
                recipeId = recipeId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Collections.route) {
            CollectionsScreen(
                onCollectionClick = { collectionId ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collectionId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: return@composable
            val collectionsViewModel: CollectionsViewModel = hiltViewModel()

            CollectionDetailScreen(
                collectionId = collectionId,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.UserRecipeDetail.createRoute(recipeId))
                },
                onBackClick = { navController.popBackStack() },
                onAddRecipeClick = {
                    navController.navigate(Screen.AddToCollection.createRoute(collectionId))
                },
                viewModel = collectionsViewModel
            )
        }

        composable(
            route = Screen.AddToCollection.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getInt("collectionId") ?: return@composable

            AddToCollectionScreen(
                collectionId = collectionId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.UserRecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: return@composable

            UserRecipeDetailScreen(
                recipeId = recipeId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.CreateEditRecipe.createRoute(id))
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
