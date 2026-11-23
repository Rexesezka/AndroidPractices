package com.example.myapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.MatchStatus

/** Счёт в виде "2 : 1" либо "— : —", если матч ещё не начался. */
fun Game.scoreText(): String =
    if (homeScore != null && awayScore != null) "$homeScore : $awayScore" else "— : —"

/** Основные коэффициенты 1X2 (Home / Draw / Away), если есть. */
fun Game.mainOdds(): Triple<Double?, Double?, Double?> {
    val market = odds.firstOrNull { it.marketId == 1 } ?: return Triple(null, null, null)
    val home = market.odds.firstOrNull { it.name.equals("Home", true) }?.value
    val draw = market.odds.firstOrNull { it.name.equals("Draw", true) }?.value
    val away = market.odds.firstOrNull { it.name.equals("Away", true) }?.value
    return Triple(home, draw, away)
}

fun Double.asXg(): String = "%.2f".format(this)

fun Double.asOdd(): String = "%.2f".format(this)

/** Вероятность в процентах. Принимает значение как 0..1, так и 0..100. */
fun Double.asProbabilityPercent(): String {
    val percent = if (this <= 1.0) this * 100 else this
    return "${percent.toInt()}%"
}

@Composable
fun StatusBadge(status: MatchStatus, modifier: Modifier = Modifier) {
    val color: Color = when (status) {
        MatchStatus.LIVE -> MaterialTheme.colorScheme.error
        MatchStatus.FINISHED -> MaterialTheme.colorScheme.primary
        MatchStatus.NOT_STARTED -> MaterialTheme.colorScheme.secondary
        MatchStatus.POSTPONED -> MaterialTheme.colorScheme.tertiary
        MatchStatus.CANCELLED -> MaterialTheme.colorScheme.outline
        MatchStatus.UNKNOWN -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = status.title, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
