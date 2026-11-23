package com.example.myapp.domain.model

/**
 * Матч, сохранённый в избранное. Хранится в локальной БД (Room),
 * поэтому доступен без подключения к интернету.
 */
data class FavoriteGame(
    val id: Int,
    val leagueName: String?,
    val seasonYear: Int?,
    val date: String?,
    val statusTitle: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
) {
    val scoreText: String
        get() = if (homeScore != null && awayScore != null) "$homeScore : $awayScore" else "— : —"
}
