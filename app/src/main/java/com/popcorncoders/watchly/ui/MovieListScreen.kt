package com.popcorncoders.watchly.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloatAsState
import com.popcorncoders.watchly.model.Movie


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    movies: List<Movie>,
    favoriteMovieIds: Set<Int>,
    errorMessage: String? = null,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    onFavoritesPageClick: () -> Unit,
    onRatedMoviesPageClick: () -> Unit,
    onSearchChanged: (String) -> Unit = {}
) {
    val searchText = remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val density = LocalDensity.current

    val filteredMovies = if (searchText.value.isBlank()) {
        movies
    } else {
        movies.filter {
            it.title.contains(searchText.value, ignoreCase = true)
        }
    }

    val layoutInfo by remember {
        derivedStateOf { listState.layoutInfo }
    }

    val totalItems = filteredMovies.size

    val visibleItems by remember {
        derivedStateOf { layoutInfo.visibleItemsInfo.size }
    }

    val scrollProgress by remember {
        derivedStateOf {
            val maxScroll = (totalItems - visibleItems).coerceAtLeast(1)
            listState.firstVisibleItemIndex.toFloat() / maxScroll.toFloat()
        }
    }

    val thumbRatio by remember {
        derivedStateOf {
            if (totalItems == 0) 1f
            else visibleItems.toFloat() / totalItems.toFloat()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) 1f else 0.4f,
        label = ""
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Watchly") },
                actions = {
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = "Toggle dark mode"
                        )
                    }

                    IconButton(onClick = onRatedMoviesPageClick) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rated movies"
                        )
                    }

                    IconButton(onClick = onFavoritesPageClick) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites page"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchText.value,
                    onValueChange = {
                        searchText.value = it
                        onSearchChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search movies") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    movies.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    filteredMovies.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No movies found.")
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filteredMovies) { movie ->
                                MovieItemCard(
                                    movie = movie,
                                    isFavorite = favoriteMovieIds.contains(movie.id),
                                    onClick = { onMovieClick(movie) },
                                    onFavoriteClick = { onFavoriteClick(movie) }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredMovies.isNotEmpty() && totalItems > visibleItems) {
                val viewportHeight = listState.layoutInfo.viewportEndOffset -
                        listState.layoutInfo.viewportStartOffset

                val thumbHeight = viewportHeight * thumbRatio
                val thumbOffset = viewportHeight * scrollProgress

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .fillMaxHeight()
                        .width(6.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Gray.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { maxOf(thumbHeight, 40f).toDp() })
                            .offset(y = with(density) { thumbOffset.toDp() })
                            .background(
                                color = if (isDarkMode)
                                    Color.White.copy(alpha = 0.7f)
                                else
                                    Color.Black.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .alpha(alpha)
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieItemCard(
    movie: Movie,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .height(92.dp)
                    .weight(0.25f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Movie poster placeholder"
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(0.60f)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap movie to view description and rate it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(0.15f),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Add to favorites",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}