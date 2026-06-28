package com.example.recipebook.data.local.UserEntities.UserColletcions

import com.example.recipebook.models.CollectionRecipe
import com.example.recipebook.models.RecipeCollection

fun CollectionEntity.toDomain(recipes: List<CollectionRecipe>): RecipeCollection =
    RecipeCollection(
        id = id,
        name = name,
        description = description,
        icon = icon,
        recipes = recipes,
        createdAt = createdAt
    )

fun RecipeCollection.toEntity(): CollectionEntity = CollectionEntity(
    id = id,
    name = name,
    description = description,
    icon = icon,
    createdAt = createdAt
)

fun CollectionRecipeEntity.toDomain(): CollectionRecipe = CollectionRecipe(
    recipeId = recipeId,
    addedAt = addedAt
)

fun CollectionRecipe.toEntity(collectionId: Int): CollectionRecipeEntity =
    CollectionRecipeEntity(
        collectionId = collectionId,
        recipeId = recipeId,
        addedAt = addedAt
    )