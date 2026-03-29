package com.example.recipebook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.recipebook.data.local.favourite.FavouriteDao
import com.example.recipebook.data.local.favourite.FavouriteMealEntity
import com.example.recipebook.data.local.history.HistoryDao
import com.example.recipebook.data.local.history.HistoryEntity
import com.example.recipebook.data.local.searchResults.SearchMealDao
import com.example.recipebook.data.local.searchResults.SearchMealEntity

@Database(
    entities = [FavouriteMealEntity::class,
                SearchMealEntity::class,
                HistoryEntity::class],
    version = RecipeBookDatabase.VERSION,
    exportSchema = false
)
abstract class RecipeBookDatabase : RoomDatabase() {

    abstract fun favouriteDao() : FavouriteDao
    abstract fun searchMealDao(): SearchMealDao

    abstract fun historyDao(): HistoryDao

    companion object {
        const val VERSION = 3
        const val NAME = "recipebook.db"
    }
}