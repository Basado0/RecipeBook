package com.example.recipebook.data.local.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = HistoryEntity.TABLE_NAME )
data class HistoryEntity(
    @PrimaryKey
    val id: Int,

    val title: String,

    val image: String,

    val viewedAt: Long
) {
    companion object {
        const val TABLE_NAME = "history"
    }
}