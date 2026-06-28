package com.example.recipebook.data.local.UserEntities.UserColletcions

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = CollectionEntity.TABLE_NAME)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val icon: String?,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TABLE_NAME = "collections"
    }
}