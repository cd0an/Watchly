package com.popcorncoders.watchly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import com.popcorncoders.watchly.model.Movie
import com.popcorncoders.watchly.ui.FavoritesScreen
import com.popcorncoders.watchly.ui.MovieDetailScreen
import com.popcorncoders.watchly.ui.MovieListScreen
import com.popcorncoders.watchly.ui.RatedMoviesScreen
import com.popcorncoders.watchly.ui.theme.WatchlyTheme
import com.popcorncoders.watchly.viewmodel.FavoriteViewModel
import com.popcorncoders.watchly.viewmodel.MovieDetailViewModel
import com.popcorncoders.watchly.viewmodel.MovieListViewModel

class MainActivity : ComponentActivity() {

    private val movieListViewModel: MovieListViewModel by viewModels()
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val movieDetailViewModel: MovieDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(false) }

            WatchlyTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                val movies by movieListViewModel.movies.collectAsStateWithLifecycle(emptyList())
                val favoriteMovieIds by movieListViewModel.favoriteMovieIds.collectAsStateWithLifecycle()
                val error by movieListViewModel.error.collectAsStateWithLifecycle()

                val favorites by favoriteViewModel.favorites.collectAsStateWithLifecycle(emptyList())
                val ratedMovies by favoriteViewModel.favorites.collectAsStateWithLifecycle(emptyList())

                val mappedMovies = movies.map { entity ->
                    Movie(
                        id = entity.id,
                        title = entity.title,
                        overview = entity.overview,
                        poster_path = entity.posterPath,
                        backdrop_path = entity.backdropPath,
                        release_date = entity.releaseDate,
                        vote_average = entity.voteAverage,
                        vote_count = 0,
                        popularity = entity.popularity,
                        genre_ids = emptyList()
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "movie_list"
                ) {
                    composable("movie_list") {
                        MovieListScreen(
                            movies = mappedMovies,
                            favoriteMovieIds = favoriteMovieIds,
                            errorMessage = error,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onMovieClick = { movie ->
                                navController.navigate("movie_detail/${movie.id}")
                            },
                            onFavoriteClick = { movie ->
                                movieListViewModel.toggleFavorite(movie)
                            },
                            onFavoritesPageClick = {
                                navController.navigate("favorites")
                            },
                            onRatedMoviesPageClick = {
                                navController.navigate("rated_movies")
                            }
                        )
                    }

                    composable(
                        route = "movie_detail/{movieId}",
                        arguments = listOf(
                            navArgument("movieId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
                        val selectedMovie: Movie? = mappedMovies.find { it.id == movieId }

                        MovieDetailScreen(
                            movie = selectedMovie,
                            viewModel = movieDetailViewModel,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onFavoritesPageClick = {
                                navController.navigate("favorites")
                            },
                            onRatedMoviesPageClick = {
                                navController.navigate("rated_movies")
                            },
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("favorites") {
                        FavoritesScreen(
                            favorites = favorites,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onRatedMoviesPageClick = {
                                navController.navigate("rated_movies")
                            },
                            onRemoveClick = { movieId ->
                                favoriteViewModel.removeFavorite(movieId)
                            },
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("rated_movies") {
                        RatedMoviesScreen(
                            ratedMovies = ratedMovies.filter { it.rating > 0 },
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            onFavoritesPageClick = {
                                navController.navigate("favorites")
                            },
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}