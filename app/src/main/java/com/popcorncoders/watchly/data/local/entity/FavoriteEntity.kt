package com.popcorncoders.watchly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val movieId: Int,
    val title: String,
    val overview: String,
    val posterPath: String,
    val addedAt: Long = System.currentTimeMillis()
)