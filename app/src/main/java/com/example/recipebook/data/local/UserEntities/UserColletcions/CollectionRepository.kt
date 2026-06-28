package com.example.recipebook.data.local.UserEntities.UserColletcions

import com.example.recipebook.models.RecipeCollection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAllCollections(): Flow<List<RecipeCollection>> =
        collectionDao.observeAllCollections().flatMapLatest { entities ->
            if (entities.isEmpty()) {
                flowOf(emptyList())
            } else {
                val flows = entities.map { entity ->
                    collectionDao.observeRecipesForCollection(entity.id).map { recipes ->
                        entity.toDomain(recipes.map { it.toDomain() })
                    }
                }
                combine(flows) { it.toList() }
            }
        }

    suspend fun getCollectionById(collectionId: Int): RecipeCollection? {
        val entity = collectionDao.getCollectionById(collectionId) ?: return null
        val recipes = collectionDao
            .getRecipesForCollection(collectionId)
            .map { it.toDomain() }
        return entity.toDomain(recipes)
    }

    suspend fun createCollection(
        name: String,
        description: String?,
        icon: String?
    ): Long {
        val entity = CollectionEntity(
            name = name,
            description = description,
            icon = icon
        )
        return collectionDao.insertCollection(entity)
    }

    suspend fun updateCollection(collection: RecipeCollection) {
        collectionDao.updateCollection(collection.toEntity())
    }

    suspend fun deleteCollection(collectionId: Int) {
        collectionDao.deleteCollection(collectionId)
    }

    suspend fun addRecipeToCollection(
        collectionId: Int,
        recipeId: Int
    ): Boolean {
        val existing = collectionDao.getRecipesForCollection(collectionId)
        if (existing.any { it.recipeId == recipeId }) {
            return false
        }

        val entity = CollectionRecipeEntity(
            collectionId = collectionId,
            recipeId = recipeId
        )
        collectionDao.addRecipeToCollection(entity)
        return true
    }

    suspend fun removeRecipeFromCollection(collectionId: Int, recipeId: Int) {
        collectionDao.removeRecipeFromCollection(collectionId, recipeId)
    }
}