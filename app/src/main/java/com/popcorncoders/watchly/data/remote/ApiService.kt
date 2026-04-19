package com.popcorncoders.watchly.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.popcorncoders.watchly.model.MovieResponse
import com.popcorncoders.watchly.model.Movie

// Defines all API endpoints
// Retrofit will automatically generate the implementation
interface ApiService {
    // Makes a GET request
    // api_key passed as a query parameter in the URL
    // suspend allows this to run asynchronously using Kotlin Coroutines
    @GET("movie/popular")
    suspend fun getPopularMovies(): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int
    ): Movie

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String
    ): MovieResponse
}

