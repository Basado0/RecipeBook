package com.example.recipebook.data.local.searchResults

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = SearchMealEntity.TABLE_NAME)
data class SearchMealEntity(
    @PrimaryKey
    val id: Int,

    val title: String,

    val image: String
) {
    companion object{
        const val TABLE_NAME = "search_meals"
    }
}
