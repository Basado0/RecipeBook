package com.example.recipebook.data.local.UserEntities.UserColletcions

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = CollectionRecipeEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId"), Index("recipeId")]
)
data class CollectionRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val collectionId: Int,
    val recipeId: Int,
    val addedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TABLE_NAME = "collection_recipes"
    }
}