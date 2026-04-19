package com.popcorncoders.watchly.ui

import com.popcorncoders.watchly.data.local.entity.FavoriteEntity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.platform.LocalDensity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<FavoriteEntity>,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onRatedMoviesPageClick: () -> Unit,
    onRemoveClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {

    val listState = rememberLazyListState()
    val density = LocalDensity.current



    val alpha by animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) 1f else 0.4f,
        label = "scrollbar_alpha"
    )

    val layoutInfo by remember {
        derivedStateOf { listState.layoutInfo }
    }

    val totalItems = favorites.size

    val visibleItems by remember {
        derivedStateOf { layoutInfo.visibleItemsInfo.size }
    }

    val viewportHeightPx by remember {
        derivedStateOf {
            layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
        }
    }


    val scrollProgress by remember {
        derivedStateOf {
            val maxScrollIndex = (totalItems - visibleItems).coerceAtLeast(1)
            (listState.firstVisibleItemIndex.toFloat() / maxScrollIndex)
                .coerceIn(0f, 1f)
        }
    }

    val thumbHeightRatio by remember {
        derivedStateOf {
            if (totalItems == 0) 1f
            else visibleItems.toFloat() / totalItems.toFloat()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode)
                                Icons.Default.Brightness7
                            else
                                Icons.Default.Brightness4,
                            contentDescription = "Theme"
                        )
                    }

                    IconButton(onClick = onRatedMoviesPageClick) {
                        Icon(Icons.Default.Star, contentDescription = "Rated")
                    }

                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Favorite, contentDescription = "Fav")
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

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorite movies yet.")
                }
            } else {

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {

                    items(favorites) { movie ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    movie.title,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { onRemoveClick(movie.movieId) }
                                ) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }


                if (totalItems > visibleItems) {

                    val thumbHeightPx = viewportHeightPx * thumbHeightRatio
                    val thumbOffsetPx = viewportHeightPx * scrollProgress

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
                                .height(with(density) { thumbHeightPx.toDp() })
                                .offset(y = with(density) { thumbOffsetPx.toDp() })
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
}