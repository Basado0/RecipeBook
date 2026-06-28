package com.example.recipebook.data.local.UserEntities.UserColletcions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    // ====== Коллекции ======

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Query("SELECT * FROM ${CollectionEntity.TABLE_NAME} ORDER BY createdAt DESC")
    fun observeAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM ${CollectionEntity.TABLE_NAME} WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Int): CollectionEntity?

    @Query("DELETE FROM ${CollectionEntity.TABLE_NAME} WHERE id = :collectionId")
    suspend fun deleteCollection(collectionId: Int)

    // ====== Рецепты в коллекциях ======

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecipeToCollection(recipe: CollectionRecipeEntity)

    @Query("SELECT * FROM ${CollectionRecipeEntity.TABLE_NAME} WHERE collectionId = :collectionId")
    fun observeRecipesForCollection(collectionId: Int): Flow<List<CollectionRecipeEntity>>

    @Query("SELECT * FROM ${CollectionRecipeEntity.TABLE_NAME} WHERE collectionId = :collectionId")
    suspend fun getRecipesForCollection(collectionId: Int): List<CollectionRecipeEntity>

    @Query("DELETE FROM ${CollectionRecipeEntity.TABLE_NAME} WHERE collectionId = :collectionId AND recipeId = :recipeId")
    suspend fun removeRecipeFromCollection(collectionId: Int, recipeId: Int)

    @Query("SELECT * FROM ${CollectionRecipeEntity.TABLE_NAME} WHERE recipeId = :recipeId")
    suspend fun getCollectionsForRecipe(recipeId: Int): List<CollectionRecipeEntity>
}