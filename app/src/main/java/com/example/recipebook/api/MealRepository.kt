package com.example.recipebook.api

import com.example.recipebook.data.local.searchResults.SearchMealDao
import com.example.recipebook.data.local.searchResults.toMeal
import com.example.recipebook.data.local.searchResults.toSearchMealEntity
import com.example.recipebook.di.NetworkModule
import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import com.example.recipebook.models.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class MealRepository @Inject constructor (
    private val api: MealApi,
    private val searchMealDao: SearchMealDao
){
    suspend fun searchMeals(query: String): List<Meal> = withContext(Dispatchers.IO) {
        val response = api.searchMeals(query = query)
        val meals = response.results.map { it.toMeal() }
        saveLastResults(meals)
        meals
    }

    private suspend fun saveLastResults(meals: List<Meal>){
        searchMealDao.deleteAll()
        val entities = meals.map { it.toSearchMealEntity() }
        searchMealDao.insertAll(entities)
    }

    suspend fun getLastCachedMeals(): List<Meal> = withContext(Dispatchers.IO) {
        searchMealDao.getAll().map { it.toMeal() }
    }

    //Flow метод (реактивное наблюдение за кэшем)
    fun observeCachedMeals(): Flow<List<Meal>> =
        searchMealDao.observeAll().map { entities ->
            entities.map { it.toMeal() }
        }

    suspend fun getRecipe(id: Int): Recipe = withContext(Dispatchers.IO) {
        try {
            val infoDeferred = async {
                try {
                    api.getRecipeInformation(id)
                } catch (e: Exception) {
                    throw Exception("Failed to load recipe info: ${e.message}")
                }
            }

            val instructionsDeferred = async {
                try {
                    api.getAnalyzedInstructions(id)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val info = infoDeferred.await()
            val instructions = instructionsDeferred.await()

            info.toRecipe(instructions)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw Exception("Failed to load recipe: ${e.message}")
        }
    }
}