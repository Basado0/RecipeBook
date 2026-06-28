package com.example.recipebook.data.local.UserEntities.UserRecipes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRecipeDao {
    // ====== Рецепты ======

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: UserRecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: UserRecipeEntity)

    @Query("SELECT * FROM ${UserRecipeEntity.TABLE_NAME} ORDER BY updatedAt DESC")
    fun observeAllRecipes(): Flow<List<UserRecipeEntity>>

    @Query("SELECT * FROM ${UserRecipeEntity.TABLE_NAME} WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: Int): UserRecipeEntity?

    @Query("DELETE FROM ${UserRecipeEntity.TABLE_NAME} WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: Int)

    // ====== Ингредиенты ======

    @Insert
    suspend fun insertIngredients(ingredients: List<UserRecipeIngredientEntity>)

    @Query("SELECT * FROM ${UserRecipeIngredientEntity.TABLE_NAME} WHERE recipeId = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: Int): List<UserRecipeIngredientEntity>

    @Query("DELETE FROM ${UserRecipeIngredientEntity.TABLE_NAME} WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Int)

    // ====== Инструкции ======

    @Insert
    suspend fun insertInstructions(instructions: List<UserRecipeInstructionEntity>)

    @Query("SELECT * FROM ${UserRecipeInstructionEntity.TABLE_NAME} WHERE recipeId = :recipeId ORDER BY stepNumber")
    suspend fun getInstructionsForRecipe(recipeId: Int): List<UserRecipeInstructionEntity>

    @Query("DELETE FROM ${UserRecipeInstructionEntity.TABLE_NAME} WHERE recipeId = :recipeId")
    suspend fun deleteInstructionsForRecipe(recipeId: Int)
}