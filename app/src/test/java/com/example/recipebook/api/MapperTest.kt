package com.example.recipebook.api

import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class MapperTest {

    @Test
    fun `mealSearchResult to meal maps all fields`() {
        val result = MealSearchResult(20,"img1","Pasta")
        val meal = result.toMeal()

        assertEquals(20,meal.id)
        assertEquals("Pasta",meal.title)
        assertEquals("img1",meal.image)
    }

    @Test
    fun `recipeInformationResponse toRecipe maps all fields`() {
        val info = RecipeInformationResponse(
            id = 1,
            title = "Carbonara",
            image = "photo.jpg",
            readyInMinutes = 25,
            summary = "Classic Italian pasta.",
            cuisines = listOf("Italian", "European"),
            extendedIngredients = listOf(
                ExtendedIngredient(name = "Spaghetti", amount = 200.0, unit = "g"),
                ExtendedIngredient(name = "Egg", amount = 2.0, unit = "pcs")
            )
        )

        val instructions = listOf(
            AnalyzedInstructionResponse(
                name = "Main",
                steps = listOf(
                    StepResponse(number = 1, step = "Boil water"),
                    StepResponse(number = 2, step = "Cook pasta")
                )
            )
        )

        val recipe = info.toRecipe(instructions)

        assertEquals(1, recipe.id)
        assertEquals("Carbonara", recipe.title)
        assertEquals("photo.jpg", recipe.image)
        assertEquals(25, recipe.readyInMinutes)
        assertEquals("Classic Italian pasta.", recipe.summary)
        assertEquals(listOf("Italian", "European"), recipe.cuisines)
        assertEquals(2, recipe.ingredients.size)
        assertEquals("Spaghetti", recipe.ingredients[0].name)
        assertEquals(200.0, recipe.ingredients[0].amount, 0.001)
        assertEquals("g", recipe.ingredients[0].unit)
        assertEquals(1, recipe.instructions.size)
        assertEquals("Main", recipe.instructions[0].name)
        assertEquals(2, recipe.instructions[0].steps.size)
        assertEquals("Boil water", recipe.instructions[0].steps[0].step)
    }

    @Test
    fun `recipeInformationResponse toRecipe with empty Instructions and Ingredients`() {
        val info = RecipeInformationResponse(
            id = 5,
            title = "Simple Dish",
            image = "",
            readyInMinutes = 10,
            summary = "",
            cuisines = emptyList(),
            extendedIngredients = emptyList()
        )

        val recipe = info.toRecipe(emptyList())

        assertEquals(5, recipe.id)
        assertTrue(recipe.ingredients.isEmpty())
        assertTrue(recipe.instructions.isEmpty())
        assertTrue(recipe.cuisines.isEmpty())
    }
}