package com.example.myapp.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.domain.model.FavoriteGame
import com.example.myapp.ui.common.EmptyState

/**
 * Экран избранного. Все данные читаются из локальной БД (Room),
 * поэтому экран доступен без интернета.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onGameClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Избранное") }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.favorites.isEmpty() -> {
                    EmptyState(
                        message = "В избранном пока пусто.\nДобавьте матч иконкой сердца в списке или на экране деталей.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(uiState.favorites, key = { it.id }) { favorite ->
                            FavoriteGameCard(
                                favorite = favorite,
                                onClick = { onGameClick(favorite.id) },
                                onRemove = { viewModel.remove(favorite.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteGameCard(
    favorite: FavoriteGame,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildString {
                        append(favorite.leagueName ?: "Лига")
                        favorite.seasonYear?.let { append(" • $it") }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = favorite.statusTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Убрать из избранного",
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = favorite.homeTeamName,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = favorite.scoreText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Text(
                    text = favorite.awayTeamName,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = favorite.date.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
