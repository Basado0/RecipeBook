package com.example.recipebook.data.local.history

import com.example.recipebook.models.Meal

fun HistoryEntity.toMeal(): Meal = Meal(
    id = id,
    title = title,
    image = image
)

fun Meal.toHistoryEntity(): HistoryEntity = HistoryEntity(
    id = id,
    title = title,
    image = image,
    viewedAt = System.currentTimeMillis()
)
