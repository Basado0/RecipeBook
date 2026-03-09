package com.example.recipebook.data.local.searchResults

import com.example.recipebook.models.Meal

fun SearchMealEntity.toMeal(): Meal = Meal(
    id = id,
    title = title,
    image = image
)

fun Meal.toSearchMealEntity(): SearchMealEntity = SearchMealEntity(
    id = id,
    title = title,
    image = image
)