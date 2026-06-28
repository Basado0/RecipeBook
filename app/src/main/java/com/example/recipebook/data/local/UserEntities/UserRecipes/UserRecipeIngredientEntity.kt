package com.example.recipebook.data.local.UserEntities.UserRecipes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = UserRecipeIngredientEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserRecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class UserRecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val name: String,
    val amount: Double,
    val unit: String
) {
    companion object {
        const val TABLE_NAME = "user_recipe_ingredients"
    }
}