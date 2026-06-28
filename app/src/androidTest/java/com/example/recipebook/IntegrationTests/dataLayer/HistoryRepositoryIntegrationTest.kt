package com.example.recipebook.IntegrationTests.dataLayer

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.recipebook.data.local.RecipeBookDatabase
import com.example.recipebook.data.local.history.HistoryDao
import com.example.recipebook.data.local.history.HistoryRepository
import com.example.recipebook.models.Meal
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryRepositoryIntegrationTest {
    private lateinit var database: RecipeBookDatabase
    private lateinit var historyDao: HistoryDao
    private lateinit var historyRepository: HistoryRepository
    private lateinit var fakeApi: FakeMealApi

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RecipeBookDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        historyDao = database.historyDao()
        historyRepository = HistoryRepository(historyDao)
        fakeApi = FakeMealApi()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addToHistory_andGetHistory_shouldReturnFromNewestToOldest() = runTest {
        val meal1 = fakeApi.getMeal(1)
        val meal2 = fakeApi.getMeal(2)

        historyRepository.addToHistory(meal1) //old
        historyRepository.addToHistory(meal2) //new

        val history = historyRepository.getHistory()
        assertEquals(2,history.size)
        assertEquals(listOf(meal2,meal1),history)
    }

    @Test
    fun addToHistory_withSameId_shouldUpdateExistingEntry() = runTest {
        val originalMeal = fakeApi.getMeal(1)
        historyRepository.addToHistory(originalMeal)

        val updatedMeal = originalMeal.copy(title = "Margherita Pizza")
        historyRepository.addToHistory(updatedMeal)

        val history = historyRepository.getHistory()
        assertEquals(1,history.size)
        assertEquals(history[0],updatedMeal)
        assertEquals(history[0].title,"Margherita Pizza")
    }

    @Test
    fun getHistory_whenEmpty_shouldReturnEmptyList() = runTest {
        val history = historyRepository.getHistory()
        assertTrue(history.isEmpty())
    }



    private class FakeMealApi {
        fun getMeal(id: Int): Meal = when (id) {
            1 -> Meal(id = 1, title = "Pizza", image = "https://example.com/pizza.jpg")
            2 -> Meal(id = 2, title = "Burger", image = "https://example.com/burger.jpg")
            else -> Meal(id = id, title = "Unknown", image = "")
        }
    }
}

