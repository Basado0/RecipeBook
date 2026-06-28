package com.example.recipebook.data.local.UserEntities.UserRecipes

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Instruction
import com.example.recipebook.models.UserRecipe

@Entity(tableName = UserRecipeEntity.TABLE_NAME)
data class UserRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val imageUri: String?,
    val description: String?,
    val readyInMinutes: Int?,
    val servings: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        const val TABLE_NAME = "user_recipes"
    }
}