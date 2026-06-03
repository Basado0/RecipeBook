package com.example.recipebook.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(historyEntity: HistoryEntity)

    @Query("SELECT * FROM ${HistoryEntity.TABLE_NAME} ORDER BY viewedAt DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    //Flow-версия для реактивного наблюдения
    @Query("SELECT * FROM ${HistoryEntity.TABLE_NAME} ORDER BY viewedAt DESC")
    fun observeHistory(): Flow<List<HistoryEntity>>
}