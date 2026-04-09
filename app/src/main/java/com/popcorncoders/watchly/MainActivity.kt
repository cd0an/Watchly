package com.popcorncoders.watchly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.popcorncoders.watchly.model.Movie
import com.popcorncoders.watchly.ui.FavoritesScreen
import com.popcorncoders.watchly.ui.MovieDetailScreen
import com.popcorncoders.watchly.ui.MovieListScreen
import com.popcorncoders.watchly.ui.theme.WatchlyTheme
import com.popcorncoders.watchly.viewmodel.MovieListViewModel

class MainActivity : ComponentActivity() {

    private val movieListViewModel: MovieListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WatchlyTheme {
                val navController = rememberNavController()

                val movies by movieListViewModel.movies.observeAsState(emptyList())
                val error by movieListViewModel.error.observeAsState()

                NavHost(
                    navController = navController,
                    startDestination = "movie_list"
                ) {
                    composable("movie_list") {
                        MovieListScreen(
                            movies = movies,
                            errorMessage = error,
                            onMovieClick = { movie ->
                                navController.navigate("movie_detail/${movie.id}")
                            },
                            onFavoritesClick = {
                                navController.navigate("favorites")
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
                        val selectedMovie: Movie? = movies.find { it.id == movieId }

                        MovieDetailScreen(
                            movie = selectedMovie,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("favorites") {
                        FavoritesScreen(
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