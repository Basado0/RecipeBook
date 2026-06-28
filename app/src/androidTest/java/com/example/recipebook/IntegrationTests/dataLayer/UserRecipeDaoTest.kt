package com.example.recipebook.IntegrationTests.dataLayer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.recipebook.data.local.RecipeBookDatabase
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeDao
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeIngredientEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRecipeDaoTest {

    private lateinit var database: RecipeBookDatabase
    private lateinit var dao: UserRecipeDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RecipeBookDatabase::class.java).build()
        dao = database.userRecipeDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndObserveRecipes() = runTest {
        val recipe = UserRecipeEntity(title = "Pizza", servings = 4, createdAt = 0L, updatedAt = 0L, imageUri = "img", description = "description", readyInMinutes = 25)
        val id = dao.insertRecipe(recipe)
        val entities = dao.observeAllRecipes().first()
        assertEquals(1, entities.size)
        assertEquals("Pizza", entities[0].title)
    }

    @Test
    fun insertAndGetIngredients() = runTest {
        val recipe = UserRecipeEntity(title = "Salad", servings = 2, createdAt = 0L, updatedAt = 0L, imageUri = "img", description = "description", readyInMinutes = 25)
        val recipeId = dao.insertRecipe(recipe)
        val ingredients = listOf(
            UserRecipeIngredientEntity(
                recipeId = recipeId.toInt(),
                name = "Tomato",
                amount = 2.0,
                unit = "pcs"
            ),
            UserRecipeIngredientEntity(recipeId = recipeId.toInt(), name = "Olive oil", amount = 1.0, unit = "tbsp")
        )
        dao.insertIngredients(ingredients)
        val saved = dao.getIngredientsForRecipe(recipeId.toInt())
        assertEquals(2, saved.size)
        assertEquals("Tomato", saved[0].name)
    }
}