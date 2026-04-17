package com.lwg.challenge.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
internal data class FavoriteEntity(
    @PrimaryKey
    val itemId: Int,
    val title: String,
    val imageUrl: String,
    val createdAt: Long,
)
