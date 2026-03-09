package com.example.recipebook.data.local.favourite

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = FavouriteMealEntity.TABLE_NAME)
data class FavouriteMealEntity(
    @PrimaryKey
    val id: Int,

    val title: String,

    val image:String
) {
    companion object {
        const val TABLE_NAME = "favourites_meals"
    }
}