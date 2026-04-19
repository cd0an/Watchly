package com.popcorncoders.watchly

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.popcorncoders.watchly.data.local.AppDatabase
import com.popcorncoders.watchly.data.local.dao.FavoriteDao
import com.popcorncoders.watchly.data.local.dao.MovieDao
import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import com.popcorncoders.watchly.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var movieDao: MovieDao
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        movieDao = db.movieDao()
        favoriteDao = db.favoriteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadMovies() = runBlocking {
        val sampleMovies = listOf(
            MovieEntity(
                id = 550,
                title = "Fight Club",
                overview = "An insomniac office worker and a devil-may-care soap maker form an underground fight club.",
                posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
            ),
            MovieEntity(
                id = 680,
                title = "Pulp Fiction",
                overview = "The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence and redemption.",
                posterPath = "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg"
            ),
            MovieEntity(
                id = 155,
                title = "The Dark Knight",
                overview = "Batman raises the stakes in his war on crime.",
                posterPath = "/qJ2tW6WMUDux911BTUgMe1ST0.jpg"
            )
        )

        movieDao.insertMovies(sampleMovies)

        val allMovies = movieDao.getAllMovies().first()

        assertEquals(3, allMovies.size)
        assertTrue(allMovies.any { it.title == "Fight Club" })
        assertTrue(allMovies.any { it.title == "Pulp Fiction" })
        assertTrue(allMovies.any { it.title == "The Dark Knight" })
    }

    @Test
    fun insertAndReadFavorites() = runBlocking {
        val favorite = FavoriteEntity(
            movieId = 550,
            title = "Fight Club",
            overview = "An insomniac office worker and a devil-may-care soap maker form an underground fight club.",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
        )

        favoriteDao.addFavorite(favorite)

        val allFavorites = favoriteDao.getAllFavorites().first()

        assertEquals(1, allFavorites.size)
        assertEquals("Fight Club", allFavorites[0].title)
        assertEquals(550, allFavorites[0].movieId)
    }

    @Test
    fun removeFavorite() = runBlocking {
        val favorite = FavoriteEntity(
            movieId = 550,
            title = "Fight Club",
            overview = "An insomniac office worker and a devil-may-care soap maker form an underground fight club.",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
        )

        favoriteDao.addFavorite(favorite)
        favoriteDao.removeFavorite(550)

        val allFavorites = favoriteDao.getAllFavorites().first()
        assertTrue(allFavorites.isEmpty())
    }

    @Test
    fun lookupFavoriteByMovieId() = runBlocking {
        val favorite = FavoriteEntity(
            movieId = 680,
            title = "Pulp Fiction",
            overview = "The lives of two mob hitmen intertwine in four tales of violence and redemption.",
            posterPath = "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg"
        )

        favoriteDao.addFavorite(favorite)

        val found = favoriteDao.getFavoriteByMovieId(680)
        val notFound = favoriteDao.getFavoriteByMovieId(999)

        assertNotNull(found)
        assertEquals("Pulp Fiction", found?.title)
        assertNull(notFound)
    }

    @Test
    fun updateRatingPersistsNewValue() = runBlocking {
        val favorite = FavoriteEntity(
            movieId = 550,
            title = "Fight Club",
            overview = "...",
            posterPath = "/test.jpg",
            rating = 0
        )

        favoriteDao.addFavorite(favorite)
        favoriteDao.updateRating(movieId = 550, rating = 4)

        val updated = favoriteDao.getFavoriteByMovieId(550)
        assertNotNull(updated)
        assertEquals(4, updated?.rating)
    }

    @Test
    fun updateRatingOverwritesPreviousRating() = runBlocking {
        val favorite = FavoriteEntity(
            movieId = 680,
            title = "Pulp Fiction",
            overview = "...",
            posterPath = "/test.jpg",
            rating = 2
        )

        favoriteDao.addFavorite(favorite)
        favoriteDao.updateRating(movieId = 680, rating = 5)
        favoriteDao.updateRating(movieId = 680, rating = 3)

        val finalState = favoriteDao.getFavoriteByMovieId(680)
        assertEquals(3, finalState?.rating)
    }

    @Test
    fun rateMovieInsertsWhenNotFavorited() = runBlocking {
        val favorite = FavoriteEntity(
            movieId = 155,
            title = "The Dark Knight",
            overview = "...",
            posterPath = "/test.jpg"
        )

        favoriteDao.rateMovie(favorite, rating = 5)

        val saved = favoriteDao.getFavoriteByMovieId(155)
        assertNotNull(saved)
        assertEquals(5, saved?.rating)
    }

    @Test
    fun clearAllMovies() = runBlocking {
        val sampleMovies = listOf(
            MovieEntity(id = 550, title = "Fight Club", overview = "...", posterPath = "/test.jpg"),
            MovieEntity(id = 680, title = "Pulp Fiction", overview = "...", posterPath = "/test2.jpg")
        )

        movieDao.insertMovies(sampleMovies)
        assertEquals(2, movieDao.getAllMovies().first().size)

        movieDao.clearAllMovies()
        assertTrue(movieDao.getAllMovies().first().isEmpty())
    }
}
