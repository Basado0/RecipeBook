package com.example.recipebook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.recipebook.data.local.UserEntities.UserColletcions.CollectionDao
import com.example.recipebook.data.local.UserEntities.UserColletcions.CollectionEntity
import com.example.recipebook.data.local.UserEntities.UserColletcions.CollectionRecipeEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeDao
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeIngredientEntity
import com.example.recipebook.data.local.UserEntities.UserRecipes.UserRecipeInstructionEntity
import com.example.recipebook.data.local.favourite.FavouriteDao
import com.example.recipebook.data.local.favourite.FavouriteMealEntity
import com.example.recipebook.data.local.history.HistoryDao
import com.example.recipebook.data.local.history.HistoryEntity
import com.example.recipebook.data.local.searchResults.SearchMealDao
import com.example.recipebook.data.local.searchResults.SearchMealEntity
import com.example.recipebook.models.CollectionRecipe

@Database(
    entities = [FavouriteMealEntity::class,
                SearchMealEntity::class,
                HistoryEntity::class,
                UserRecipeEntity::class,
                UserRecipeIngredientEntity::class,
                UserRecipeInstructionEntity::class,
                CollectionEntity::class,
                CollectionRecipeEntity::class],
    version = RecipeBookDatabase.VERSION,
    exportSchema = false
)
abstract class RecipeBookDatabase : RoomDatabase() {

    abstract fun favouriteDao() : FavouriteDao
    abstract fun searchMealDao(): SearchMealDao

    abstract fun historyDao(): HistoryDao

    abstract fun userRecipeDao(): UserRecipeDao

    abstract fun collectionDao(): CollectionDao

    companion object {
        const val VERSION = 7
        const val NAME = "recipebook.db"
    }
}