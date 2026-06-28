package com.example.recipebook.data.local.searchResults

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchMealDao {
    @Query("DELETE FROM ${SearchMealEntity.TABLE_NAME}")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<SearchMealEntity>)

    @Query("SELECT * FROM ${SearchMealEntity.TABLE_NAME}")
    suspend fun getAll(): List<SearchMealEntity>

    // Flow-версия (реактивное наблюдение за кэшем)
    @Query("SELECT * FROM ${SearchMealEntity.TABLE_NAME}")
    fun observeAll(): Flow<List<SearchMealEntity>> // Flow вместо suspend

}