package com.example.recipebook.models

data class Meal(
    val id: Int,
    val title: String,
    val image: String,
)

data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String
)

data class Instruction(
    val name: String?,
    val steps: List<Step>
)
data class Step(
    val number: Int,
    val step: String
)

data class Recipe(
    val id: Int,
    val title: String,
    val image: String,
    val readyInMinutes: Int,
    val summary: String,
    val cuisines: List<String>,
    val ingredients: List<Ingredient>,
    val instructions: List<Instruction>
)

fun Recipe.toMeal(): Meal = Meal(
    id = id,
    title = title,
    image = image
)

data class UserRecipe(
    val id: Int = 0,
    val title: String,
    val imageUri: String?,
    val description: String?,
    val readyInMinutes: Int?,
    val servings: Int,
    val ingredients: List<Ingredient>,
    val instructions: List<Instruction>,
    val createdAt: Long,
    val updatedAt: Long
)

data class RecipeCollection(
    val id: Int = 0,
    val name: String,
    val description: String?,
    val icon: String?,
    val recipes: List<CollectionRecipe> = emptyList(),
    val createdAt: Long
)

data class CollectionRecipe(
    val recipeId: Int,
    val addedAt: Long
)

data class DisplayRecipe(
    val title: String,
    val image: String,  // URL или URI
    val readyInMinutes: Int?,
    val cuisines: List<String>,
    val summary: String,
    val ingredients: List<Ingredient>,
    val instructions: List<Instruction>
)

// Маппинг из API Recipe
fun Recipe.toDisplayRecipe(): DisplayRecipe = DisplayRecipe(
    title = title,
    image = image,
    readyInMinutes = readyInMinutes,
    cuisines = cuisines,
    summary = summary,
    ingredients = ingredients,
    instructions = instructions
)

// Маппинг из UserRecipe
fun UserRecipe.toDisplayRecipe(): DisplayRecipe = DisplayRecipe(
    title = title,
    image = imageUri ?: "",
    readyInMinutes = readyInMinutes,
    cuisines = emptyList(),
    summary = description ?: "",
    ingredients = ingredients,
    instructions = instructions
)

