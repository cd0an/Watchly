package com.popcorncoders.watchly.repository

import com.popcorncoders.watchly.data.local.dao.FavoriteDao
import com.popcorncoders.watchly.data.local.dao.MovieDao
import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import com.popcorncoders.watchly.data.local.entity.MovieEntity
import com.popcorncoders.watchly.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val apiService: ApiService,
    private val movieDao: MovieDao,
    private val favoriteDao: FavoriteDao
) {
    // Fetch movies from API and cache them in Room
    suspend fun fetchAndCacheMovies() {
        val response = apiService.getPopularMovies()
        val entities = response.results.map { movie ->
            MovieEntity (
                id = movie.id,
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.poster_path,
                backdropPath = movie.backdrop_path,
                releaseDate = movie.release_date,
                voteAverage = movie.vote_average,
                popularity = movie.popularity
            )
        }
        movieDao.clearAllMovies()
        movieDao.insertMovies(entities)
    }

    fun getMoviesFromDb(): Flow<List<MovieEntity>> {
        return movieDao.getAllMovies()
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun getFavoriteByMovieId(movieId: Int): FavoriteEntity? {
        return favoriteDao.getFavoriteByMovieId(movieId)
    }

    suspend fun upsertFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun deleteFavorite(movieId: Int) {
        favoriteDao.deleteFavorite(movieId)
    }
}