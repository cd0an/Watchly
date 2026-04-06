package com.popcorncoders.watchly

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.popcorncoders.watchly.data.local.AppDatabase
import com.popcorncoders.watchly.data.local.dao.FavoriteDao
import com.popcorncoders.watchly.data.local.dao.MovieDao
import com.popcorncoders.watchly.data.local.dao.WatchlistDao
import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import com.popcorncoders.watchly.data.local.entity.MovieEntity
import com.popcorncoders.watchly.data.local.entity.WatchlistEntity
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
    private lateinit var watchlistDao: WatchlistDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        movieDao = db.movieDao()
        favoriteDao = db.favoriteDao()
        watchlistDao = db.watchlistDao()
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
    fun insertAndReadWatchlist() = runBlocking {
        val watchlistItem = WatchlistEntity(
            movieId = 155,
            title = "The Dark Knight",
            overview = "Batman raises the stakes in his war on crime.",
            posterPath = "/qJ2tW6WMUDux911BTUgMe1ST0.jpg"
        )

        watchlistDao.addToWatchlist(watchlistItem)

        val allItems = watchlistDao.getAllWatchlistItems().first()

        assertEquals(1, allItems.size)
        assertEquals("The Dark Knight", allItems[0].title)
        assertEquals(155, allItems[0].movieId)
    }

    @Test
    fun removeFromWatchlist() = runBlocking {
        val watchlistItem = WatchlistEntity(
            movieId = 155,
            title = "The Dark Knight",
            overview = "Batman raises the stakes in his war on crime.",
            posterPath = "/qJ2tW6WMUDux911BTUgMe1ST0.jpg"
        )

        watchlistDao.addToWatchlist(watchlistItem)
        watchlistDao.removeFromWatchlist(155)

        val allItems = watchlistDao.getAllWatchlistItems().first()
        assertTrue(allItems.isEmpty())
    }

    @Test
    fun lookupWatchlistItemByMovieId() = runBlocking {
        val watchlistItem = WatchlistEntity(
            movieId = 550,
            title = "Fight Club",
            overview = "An insomniac office worker and a devil-may-care soap maker form an underground fight club.",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
        )

        watchlistDao.addToWatchlist(watchlistItem)

        val found = watchlistDao.getWatchlistItemByMovieId(550)
        val notFound = watchlistDao.getWatchlistItemByMovieId(999)

        assertNotNull(found)
        assertEquals("Fight Club", found?.title)
        assertNull(notFound)
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
