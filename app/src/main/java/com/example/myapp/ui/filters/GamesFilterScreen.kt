package com.example.myapp.ui.filters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.domain.model.GamesFilter

/**
 * Экран настроек фильтра списка матчей (не более трёх настроек):
 * статус матча, лига и год. По «Готово» настройки сохраняются в DataStore,
 * и список перезагружается с новыми параметрами.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesFilterScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: GamesFilterViewModel = viewModel(factory = GamesFilterViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки фильтра") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Настройки сохраняются на устройстве и применяются при следующем запуске.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(text = "1. Статус матча", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GamesFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = filter == uiState.filter,
                        onClick = { viewModel.selectFilter(filter) },
                        label = { Text(filter.title) },
                    )
                }
            }

            Text(text = "2. Лига (LeagueId)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = uiState.leagueId,
                onValueChange = viewModel::updateLeagueId,
                label = { Text("Например, 39") },
                supportingText = { Text("Пустое поле — любая лига") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(text = "3. Год сезона", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = uiState.year,
                onValueChange = viewModel::updateYear,
                label = { Text("Например, 2024") },
                supportingText = { Text("Пустое поле — любой год") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { viewModel.save(onSaved = onDone) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Готово")
            }

            OutlinedButton(
                onClick = viewModel::reset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сбросить настройки")
            }
        }
    }
}
