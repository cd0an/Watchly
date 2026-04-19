package com.popcorncoders.watchly.model

// Data class representing a single movie from the API
// Variable names must match the JSON response from TMDb
data class Movie (
    val id: Int, // Unique ID of the movie
    val title: String, // Movie title
    val overview: String, // Movie description or summary
    val poster_path: String?, // Path to the movie poster image
    val backdrop_path: String?, // Path to the backdrop/banner image
    val release_date: String?, // Release date e.g. "2024-07-15"
    val vote_average: Double, // TMDb average rating score e.g. 7.4
    val vote_count: Int, // Number of votes submitted on TMDb
    val popularity: Double, // TMDb popularity score based on views, votes, and watchlist adds
    val genre_ids: List<Int> // List of genres IDs e.g. [28, 12]
)
