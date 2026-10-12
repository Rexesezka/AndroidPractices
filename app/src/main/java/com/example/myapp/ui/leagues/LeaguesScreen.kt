package com.example.myapp.ui.leagues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.common.EmptyState
import com.example.myapp.ui.common.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesScreen(
    viewModel: LeaguesViewModel = viewModel(factory = LeaguesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лиги") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && uiState.leagues.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null && uiState.leagues.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage.orEmpty(),
                        onRetry = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.leagues.isEmpty() -> {
                    EmptyState(
                        message = "Лиги не найдены",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isFromCache) {
                            item {
                                Text(
                                    text = "Данные из кэша",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        }
                        items(uiState.leagues, key = { it.id }) { league ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = league.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = league.country?.name ?: "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
