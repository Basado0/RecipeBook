package com.example.recipebook.data.local.favourite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipebook.data.local.favourite.FavouriteMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {

    @Query("SELECT * FROM ${FavouriteMealEntity.TABLE_NAME}")
    fun observeAll(): Flow<List<FavouriteMealEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(meal: FavouriteMealEntity)

    @Query("DELETE FROM ${FavouriteMealEntity.TABLE_NAME} WHERE id = :mealId")
    suspend fun deleteById(mealId: Int)
}