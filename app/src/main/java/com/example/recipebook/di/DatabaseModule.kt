package com.example.recipebook.di

import android.content.Context
import androidx.room.Room
import com.example.recipebook.data.local.favourite.FavouriteDao
import com.example.recipebook.data.local.RecipeBookDatabase
import com.example.recipebook.data.local.history.HistoryDao
import com.example.recipebook.data.local.searchResults.SearchMealDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeBookDatabase =
        Room.databaseBuilder(
            context,
            RecipeBookDatabase::class.java,
            RecipeBookDatabase.NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavouriteDao(database: RecipeBookDatabase): FavouriteDao = database.favouriteDao()

    @Provides
    fun provideSearchMealDao(database: RecipeBookDatabase): SearchMealDao = database.searchMealDao()

    @Provides
    fun provideHistoryDao(database: RecipeBookDatabase): HistoryDao = database.historyDao()
}