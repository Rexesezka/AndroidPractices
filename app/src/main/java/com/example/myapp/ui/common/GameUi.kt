package com.example.myapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.model.Game
import com.example.myapp.data.model.GameStatus

fun Game.scoreText(): String {
    return if (homeScore != null && awayScore != null) {
        "$homeScore : $awayScore"
    } else {
        "— : —"
    }
}

fun Double.asXg(): String = "%.2f".format(this)

fun Double.asOdd(): String = "%.2f".format(this)

@Composable
fun StatusBadge(status: GameStatus, modifier: Modifier = Modifier) {
    val label: String
    val color: androidx.compose.ui.graphics.Color
    when (status) {
        GameStatus.SCHEDULED -> {
            label = "Запланирован"
            color = MaterialTheme.colorScheme.secondary
        }
        GameStatus.LIVE -> {
            label = "В игре"
            color = MaterialTheme.colorScheme.error
        }
        GameStatus.FINISHED -> {
            label = "Завершён"
            color = MaterialTheme.colorScheme.primary
        }
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
