package com.popcorncoders.watchly.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.popcorncoders.watchly.data.local.AppDatabase
import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import com.popcorncoders.watchly.data.local.entity.MovieEntity
import com.popcorncoders.watchly.data.remote.RetrofitClient
import com.popcorncoders.watchly.model.Movie
import com.popcorncoders.watchly.repository.MovieRepository
import com.popcorncoders.watchly.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MovieListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private var notificationShown = false

    private val repository = MovieRepository(
        apiService = RetrofitClient.api,
        movieDao = database.movieDao(),
        favoriteDao = database.favoriteDao(),
        ratingDao = database.ratingDao()
    )

    // StateFlow for the list of movies
    private val _movies = MutableStateFlow<List<MovieEntity>>(emptyList())
    val movies: StateFlow<List<MovieEntity>> = _movies

    // StateFlow for favorite movie IDs
    private val _favoriteMovieIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteMovieIds: StateFlow<Set<Int>> = _favoriteMovieIds

    // StateFlow for errors
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchMovies()
        observeFavorites()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                repository.fetchAndCacheMovies()
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("MovieListViewModel", "Error fetching movies", e)
            }

            repository.getMoviesFromDb().collectLatest { cachedMovies ->
                _movies.value = cachedMovies
                Log.d("MovieListViewModel", "Movies from DB: ${cachedMovies.size}")

                // Show notification once movies are loaded
                if (cachedMovies.isNotEmpty() && !notificationShown) {
                    notificationShown = true
                    NotificationHelper.showNewMoviesNotification(
                        context = getApplication(),
                        movieCount = cachedMovies.size
                    )
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavorites().collectLatest { favorites ->
                _favoriteMovieIds.value = favorites.map { it.movieId }.toSet()
            }
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            val existing = repository.getFavoriteByMovieId(movie.id)

            if (existing == null) {
                repository.upsertFavorite(
                    FavoriteEntity(
                        movieId = movie.id,
                        title = movie.title,
                        overview = movie.overview,
                        posterPath = movie.poster_path,
                        backdropPath = movie.backdrop_path,
                        releaseDate = movie.release_date,
                        voteAverage = movie.vote_average
                    )
                )
            } else {
                repository.deleteFavorite(movie.id)
            }
        }
    }
}