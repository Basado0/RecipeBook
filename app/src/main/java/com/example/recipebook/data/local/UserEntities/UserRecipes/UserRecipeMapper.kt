package com.example.recipebook.data.local.UserEntities.UserRecipes

import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Instruction
import com.example.recipebook.models.Step
import com.example.recipebook.models.UserRecipe

fun UserRecipeEntity.toDomain(
    ingredients: List<Ingredient>,
    instructions: List<Instruction>
): UserRecipe = UserRecipe(
    id = id,
    title = title,
    imageUri = imageUri,
    description = description,
    readyInMinutes = readyInMinutes,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserRecipe.toEntity(): UserRecipeEntity = UserRecipeEntity(
    id = id,
    title = title,
    imageUri = imageUri,
    description = description,
    readyInMinutes = readyInMinutes,
    servings = servings,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)

fun Ingredient.toEntity(recipeId: Int): UserRecipeIngredientEntity =
    UserRecipeIngredientEntity(
        recipeId = recipeId,
        name = name,
        amount = amount,
        unit = unit
    )

fun UserRecipeIngredientEntity.toDomain(): Ingredient = Ingredient(
    name = name,
    amount = amount,
    unit = unit
)

fun Instruction.toEntities(recipeId: Int): List<UserRecipeInstructionEntity> =
    steps.map { step ->
        UserRecipeInstructionEntity(
            recipeId = recipeId,
            stepNumber = step.number,
            description = "${name ?: ""}: ${step.step}".trimStart(':').trim()
        )
    }

fun List<UserRecipeInstructionEntity>.toDomain(): List<Instruction> {
    return this.groupBy { entity ->
        entity.description.split(":").firstOrNull()?.trim() ?: "Instructions"
    }.map { (name, entities) ->
        Instruction(
            name = name.takeIf { it != "Instructions" },
            steps = entities.map { entity ->
                Step(
                    number = entity.stepNumber,
                    step = entity.description.substringAfter(":").trimStart()
                        .ifEmpty { entity.description }
                )
            }
        )
    }
}