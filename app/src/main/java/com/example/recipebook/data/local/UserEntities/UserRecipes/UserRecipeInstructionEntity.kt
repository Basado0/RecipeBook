package com.example.recipebook.data.local.UserEntities.UserRecipes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = UserRecipeInstructionEntity.TABLE_NAME,
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
data class UserRecipeInstructionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val stepNumber: Int,
    val description: String
) {
    companion object {
        const val TABLE_NAME = "user_recipe_instructions"
    }
}