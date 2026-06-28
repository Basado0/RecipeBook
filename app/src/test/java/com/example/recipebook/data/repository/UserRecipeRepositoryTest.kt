package com.example.recipebook.data.repository

import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeDao
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeIngredientEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeInstructionEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeRepository
import com.example.recipebook.models.Ingredient
import com.example.recipebook.models.Instruction
import com.example.recipebook.models.Step
import com.example.recipebook.models.UserRecipe
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@RunWith(MockitoJUnitRunner::class)
class UserRecipeRepositoryTest {

    private lateinit var dao: UserRecipeDao
    private lateinit var repository: UserRecipeRepository

    @Before
    fun setUp() {
        dao = mock()
        repository = UserRecipeRepository(dao)
    }

    @Test
    fun `saveUserRecipe inserts recipe and returns id`() = runTest {
        val recipe = UserRecipe(
            title = "Test",
            servings = 2,
            ingredients = listOf(Ingredient("salt", 1.0, "tsp")),
            instructions = listOf(Instruction("Mix", listOf(Step(1, "mix")))),
            createdAt = 0L,
            updatedAt = 0L,
            imageUri = "https://test/image",
            description = "Description",
            readyInMinutes = 25
        )

        doReturn(1L).whenever(dao).insertRecipe(any<UserRecipeEntity>())
        doReturn(Unit).whenever(dao).insertIngredients(any<List<UserRecipeIngredientEntity>>())
        doReturn(Unit).whenever(dao).insertInstructions(any<List<UserRecipeInstructionEntity>>())

        val id = repository.saveUserRecipe(recipe)
        assertEquals(1L, id)

        verify(dao).insertRecipe(any<UserRecipeEntity>())
        verify(dao).insertIngredients(any<List<UserRecipeIngredientEntity>>())
        verify(dao).insertInstructions(any<List<UserRecipeInstructionEntity>>())
    }

    @Test
    fun `observeAllUserRecipes returns mapped domain models`() = runTest {
        val entity = UserRecipeEntity(1, "Test", null, null, 30, 4, 0L, 0L)
        val ingEntity = UserRecipeIngredientEntity(1, 1, "salt", 1.0, "tsp")
        val instrEntity = UserRecipeInstructionEntity(1, 1, 1, "Mix: mix")

        whenever(dao.observeAllRecipes()).thenReturn(flowOf(listOf(entity)))

        // getIngredientsForRecipe и getInstructionsForRecipe — suspend → doReturn + whenever
        doReturn(listOf(ingEntity)).whenever(dao).getIngredientsForRecipe(any())
        doReturn(listOf(instrEntity)).whenever(dao).getInstructionsForRecipe(any())

        val list = repository.observeAllUserRecipes().first()
        assertEquals(1, list.size)
        assertEquals("Test", list[0].title)
        assertEquals(1, list[0].ingredients.size)
        assertEquals("salt", list[0].ingredients[0].name)

        verify(dao).observeAllRecipes()
        verify(dao).getIngredientsForRecipe(any())
        verify(dao).getInstructionsForRecipe(any())
    }

    @Test
    fun `deleteUserRecipe deletes recipe and related data`() = runTest {
        // Все методы удаления – suspend, ставим стабы на Unit
        doReturn(Unit).whenever(dao).deleteIngredientsForRecipe(any())
        doReturn(Unit).whenever(dao).deleteInstructionsForRecipe(any())
        doReturn(Unit).whenever(dao).deleteRecipeById(any())

        repository.deleteUserRecipe(1)

        verify(dao).deleteIngredientsForRecipe(1)
        verify(dao).deleteInstructionsForRecipe(1)
        verify(dao).deleteRecipeById(1)
    }
}