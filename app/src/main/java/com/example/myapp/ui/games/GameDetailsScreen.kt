package com.example.myapp.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.data.model.Game
import com.example.myapp.data.model.MatchEvent
import com.example.myapp.ui.common.ErrorState
import com.example.myapp.ui.common.StatusBadge
import com.example.myapp.ui.common.asOdd
import com.example.myapp.ui.common.asXg
import com.example.myapp.ui.common.scoreText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    gameId: Int,
    onBack: () -> Unit,
    viewModel: GameDetailsViewModel = viewModel(factory = GameDetailsViewModel.factory(gameId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.game?.league?.name ?: "Матч") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val game = uiState.game
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                game == null -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Матч не найден",
                        onRetry = viewModel::load,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    GameDetailsContent(game = game)
                }
            }
        }
    }
}

@Composable
private fun GameDetailsContent(game: Game) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScoreboardCard(game = game)
        PredictionsCard(game = game)
        OddsCard(game = game)
        if (game.events.isNotEmpty()) {
            EventsCard(events = game.events)
        }
    }
}

@Composable
private fun ScoreboardCard(game: Game) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            val (leagueRef, homeRef, scoreRef, awayRef, statusRef, dateRef) = createRefs()

            Text(
                text = "${game.league.name} • ${game.league.season}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(leagueRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            )

            Text(
                text = game.homeTeam.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(homeRef) {
                    top.linkTo(leagueRef.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(scoreRef.start, margin = 12.dp)
                    width = Dimension.fillToConstraints
                },
            )

            Text(
                text = game.scoreText(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(scoreRef) {
                    top.linkTo(leagueRef.bottom, margin = 14.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            )

            Text(
                text = game.awayTeam.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(awayRef) {
                    top.linkTo(leagueRef.bottom, margin = 20.dp)
                    start.linkTo(scoreRef.end, margin = 12.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            )

            StatusBadge(
                status = game.status,
                modifier = Modifier.constrainAs(statusRef) {
                    top.linkTo(scoreRef.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            )

            Text(
                text = game.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(dateRef) {
                    top.linkTo(statusRef.bottom, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            )
        }
    }
}

@Composable
private fun PredictionsCard(game: Game) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Прогноз Glicko 2 (xG)", style = MaterialTheme.typography.titleMedium)
            StatRow(label = game.homeTeam.name, value = game.homeXg.asXg())
            StatRow(label = game.awayTeam.name, value = game.awayXg.asXg())
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OddsCard(game: Game) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Коэффициенты", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OddItem(label = "П1", value = game.oddsHome.asOdd())
                OddItem(label = "X", value = game.oddsDraw.asOdd())
                OddItem(label = "П2", value = game.oddsAway.asOdd())
            }
        }
    }
}

@Composable
private fun OddItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EventsCard(events: List<MatchEvent>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "События матча", style = MaterialTheme.typography.titleMedium)
            events.forEach { event ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${event.minute}'",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(44.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = event.type.label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = event.playerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = event.teamName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
