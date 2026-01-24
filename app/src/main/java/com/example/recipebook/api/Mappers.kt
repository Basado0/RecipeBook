package com.example.recipebook.api

import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Instruction
import com.example.recipebook.models.Meal
import com.example.recipebook.models.Recipe
import com.example.recipebook.models.Step

fun MealSearchResult.toMeal(): Meal{
    return Meal(
        id = id,
        title = title,
        image = image
    )
}

fun ExtendedIngredient.toIngredient(): Ingredient{
    return Ingredient(
        name=name,
        amount=amount,
        unit=unit
    )
}

fun StepResponse.toStep(): Step{
    return Step(
        number = number,
        step = step
    )
}

fun AnalyzedInstructionResponse.toInstruction(): Instruction{
    return Instruction(
        name=name,
        steps=steps.map { it.toStep() }
    )
}

fun RecipeInformationResponse.toRecipe(
    instructions: List<AnalyzedInstructionResponse> = emptyList()
): Recipe{
    return Recipe(
        id=id,
        title=title,
        image=image,
        readyInMinutes=readyInMinutes,
        summary=summary,
        cuisines=cuisines,
        ingredients = extendedIngredients.map { it.toIngredient() },
        instructions = instructions.map { it.toInstruction() }
    )
}