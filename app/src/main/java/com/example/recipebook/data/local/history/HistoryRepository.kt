package com.example.recipebook.data.local.history

import com.example.recipebook.models.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    suspend fun addToHistory(meal: Meal) {
        historyDao.insertOrUpdate(meal.toHistoryEntity())
    }

    suspend fun getHistory(): List<Meal> {
        return historyDao.getAllHistory().map { it.toMeal() }
    }

    // Flow метод для реактивного наблюдения за историей
    fun observeHistory(): Flow<List<Meal>> =
        historyDao.observeHistory().map { entities ->
            entities.map { it.toMeal() }
        }
}