package com.popcorncoders.watchly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.popcorncoders.watchly.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(watchlistItem: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun removeFromWatchlist(movieId: Int)

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllWatchlistItems(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE movieId = :movieId LIMIT 1")
    suspend fun getWatchlistItemByMovieId(movieId: Int): WatchlistEntity?
}