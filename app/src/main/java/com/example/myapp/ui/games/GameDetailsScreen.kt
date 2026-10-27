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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.GameDetails
import com.example.myapp.domain.model.GlickoPrediction
import com.example.myapp.domain.model.MatchEvent
import com.example.myapp.domain.model.OddsMarket
import com.example.myapp.domain.model.StatLine
import com.example.myapp.ui.common.ErrorState
import com.example.myapp.ui.common.StatusBadge
import com.example.myapp.ui.common.asOdd
import com.example.myapp.ui.common.asProbabilityPercent
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
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.details?.game?.leagueName ?: "Матч") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::toggleFavorite,
                        enabled = uiState.details != null,
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = if (isFavorite) {
                                "Убрать из избранного"
                            } else {
                                "Добавить в избранное"
                            },
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
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
            val details = uiState.details
            when {
                uiState.isLoading && details == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                details == null -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Не удалось загрузить матч",
                        onRetry = viewModel::load,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    GameDetailsContent(details = details)
                }
            }
        }
    }
}

@Composable
private fun GameDetailsContent(details: GameDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScoreboardCard(game = details.game)
        details.glicko?.let { PredictionsCard(game = details.game, glicko = it) }
        if (details.statistics.isNotEmpty()) {
            StatisticsCard(statistics = details.statistics)
        }
        if (details.events.isNotEmpty()) {
            EventsCard(game = details.game, events = details.events)
        }
        if (details.game.odds.isNotEmpty()) {
            OddsCard(odds = details.game.odds)
        }
        VenueCard(details = details)
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
                text = buildString {
                    append(game.leagueName ?: "Лига")
                    game.seasonYear?.let { append(" • сезон $it") }
                    game.roundName?.let { append(" • $it") }
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(leagueRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
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
                text = game.date.orEmpty(),
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
private fun PredictionsCard(game: Game, glicko: GlickoPrediction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "Прогноз Glicko 2 и xG", style = MaterialTheme.typography.titleMedium)

            StatRow(label = game.homeTeam.name, value = glicko.homeRating?.let { "%.0f".format(it) } ?: "—")
            StatRow(label = game.awayTeam.name, value = glicko.awayRating?.let { "%.0f".format(it) } ?: "—")

            HorizontalDivider()

            StatRow(label = "xG ${game.homeTeam.name}", value = glicko.homeXg?.asXg() ?: "—")
            StatRow(label = "xG ${game.awayTeam.name}", value = glicko.awayXg?.asXg() ?: "—")

            glicko.homeWinProbability?.let { home ->
                HorizontalDivider()
                StatRow(label = "Вероятность победы хозяев", value = home.asProbabilityPercent())
                glicko.awayWinProbability?.let { away ->
                    StatRow(label = "Вероятность победы гостей", value = away.asProbabilityPercent())
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(statistics: List<StatLine>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "Статистика матча", style = MaterialTheme.typography.titleMedium)
            statistics.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = line.home,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(56.dp),
                    )
                    Text(
                        text = line.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = line.away,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(56.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EventsCard(game: Game, events: List<MatchEvent>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "События матча", style = MaterialTheme.typography.titleMedium)
            events.forEach { event ->
                val teamName = when (event.teamId) {
                    game.homeTeam.id -> game.homeTeam.name
                    game.awayTeam.id -> game.awayTeam.name
                    else -> null
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = event.minuteLabel,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(48.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = event.type.title, style = MaterialTheme.typography.bodyMedium)
                        val subtitle = listOfNotNull(event.playerName, event.assistName?.let { "ассист: $it" })
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    teamName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OddsCard(odds: List<OddsMarket>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "Коэффициенты", style = MaterialTheme.typography.titleMedium)
            odds.forEach { market ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = market.marketName ?: "Рынок ${market.marketId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = market.odds.joinToString("  ") { "${it.name} ${it.value.asOdd()}" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueCard(details: GameDetails) {
    val venue = details.venue
    if (venue?.name == null && details.refereeName == null) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "Матч-центр", style = MaterialTheme.typography.titleMedium)
            venue?.name?.let { StatRow(label = "Стадион", value = it) }
            venue?.city?.let { StatRow(label = "Город", value = it) }
            venue?.capacity?.let { StatRow(label = "Вместимость", value = it.toString()) }
            details.refereeName?.let { StatRow(label = "Судья", value = it) }
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
