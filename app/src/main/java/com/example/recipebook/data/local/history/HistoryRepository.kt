package com.example.recipebook.data.local.history

import com.example.recipebook.models.Meal
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
}