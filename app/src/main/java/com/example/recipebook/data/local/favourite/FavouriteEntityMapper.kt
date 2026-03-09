package com.example.recipebook.data.local.favourite

import com.example.recipebook.data.local.favourite.FavouriteMealEntity
import com.example.recipebook.models.Meal

fun FavouriteMealEntity.toMeal() : Meal = Meal(
    id = id,
    title = title,
    image = image
)

fun Meal.toFavouriteEntity() : FavouriteMealEntity = FavouriteMealEntity(
    id = id,
    title = title,
    image = image
)