package com.popcorncoders.watchly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: Int)

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE movieId = :movieId LIMIT 1")
    suspend fun getFavoriteByMovieId(movieId: Int): FavoriteEntity?

    @Query("UPDATE favorites SET rating = :rating WHERE movieId = :movieId")
    suspend fun updateRating(movieId: Int, rating: Int)

    @Transaction
    suspend fun rateMovie(favorite: FavoriteEntity, rating: Int) {
        val existing = getFavoriteByMovieId(favorite.movieId)
        if (existing == null) {
            addFavorite(favorite.copy(rating = rating))
        } else {
            updateRating(favorite.movieId, rating)
        }
    }
}