package com.example.recipebook.api

import com.example.recipebook.network.NetworkModule
import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import com.example.recipebook.models.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MealRepository(private val api: MealApi = NetworkModule.api){
    suspend fun searchMeals(query: String): List<Meal> = withContext(Dispatchers.IO) {
        api.searchMeals(query = query).results.map { it.toMeal() }
    }
    suspend fun getRecipe(id: Int): Recipe = withContext(Dispatchers.IO) {
        val infoDeferred = async {api.getRecipeInformation(id)}
        val instructionsDeferred = async { api.getAnalyzedInstructions(id) }

        val info = infoDeferred.await()
        val instructions = instructionsDeferred.await()

        info.toRecipe(instructions)
    }
}