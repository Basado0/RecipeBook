package com.example.recipebook.data.local.UserEntities.UserRecipes

import com.example.recipebook.models.UserRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRecipeRepository @Inject constructor(
    private val userRecipeDao: UserRecipeDao
){

    // Получить Flow всех своих рецептов
    fun observeAllUserRecipes(): Flow<List<UserRecipe>> =
        userRecipeDao.observeAllRecipes().map { entities ->
            entities.map { entity ->
                val ingredients = userRecipeDao
                    .getIngredientsForRecipe(entity.id)
                    .map { it.toDomain() }
                val instructions = userRecipeDao
                    .getInstructionsForRecipe(entity.id)
                    .toDomain()
                entity.toDomain(ingredients, instructions)
            }
        }

    // Получить рецепт по ID
    suspend fun getUserRecipeById(recipeId: Int): UserRecipe? {
        val entity = userRecipeDao.getRecipeById(recipeId) ?: return null
        val ingredients = userRecipeDao
            .getIngredientsForRecipe(recipeId)
            .map { it.toDomain() }
        val instructions = userRecipeDao
            .getInstructionsForRecipe(recipeId)
            .toDomain()
        return entity.toDomain(ingredients, instructions)
    }

    // Сохранить новый рецепт
    suspend fun saveUserRecipe(recipe: UserRecipe): Long {
        val recipeId = userRecipeDao.insertRecipe(recipe.toEntity())

        // Сохраняем ингредиенты
        val ingredientEntities = recipe.ingredients.map { it.toEntity(recipeId.toInt()) }
        userRecipeDao.insertIngredients(ingredientEntities)

        // Сохраняем инструкции
        val instructionEntities = recipe.instructions.flatMap { it.toEntities(recipeId.toInt()) }
        userRecipeDao.insertInstructions(instructionEntities)

        return recipeId
    }

    // Обновить существующий рецепт
    suspend fun updateUserRecipe(recipe: UserRecipe) {
        userRecipeDao.updateRecipe(recipe.toEntity())

        // Удаляем старые ингредиенты и инструкции
        userRecipeDao.deleteIngredientsForRecipe(recipe.id)
        userRecipeDao.deleteInstructionsForRecipe(recipe.id)

        // Вставляем новые
        val ingredientEntities = recipe.ingredients.map { it.toEntity(recipe.id) }
        userRecipeDao.insertIngredients(ingredientEntities)

        val instructionEntities = recipe.instructions.flatMap { it.toEntities(recipe.id) }
        userRecipeDao.insertInstructions(instructionEntities)
    }

    // Удалить рецепт
    suspend fun deleteUserRecipe(recipeId: Int) {
        userRecipeDao.deleteIngredientsForRecipe(recipeId)
        userRecipeDao.deleteInstructionsForRecipe(recipeId)
        userRecipeDao.deleteRecipeById(recipeId)
    }
}