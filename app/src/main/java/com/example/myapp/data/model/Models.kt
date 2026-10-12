package com.example.myapp.data.model

/**
 * Доменные модели футбольной статистики (sstats.net).
 * Отражают структуру ответов /games, /teams, /players, /leagues.
 */

enum class GameStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
}

data class League(
    val id: Int,
    val name: String,
    val country: String,
    val season: String,
)

data class Team(
    val id: Int,
    val name: String,
    val country: String,
)

data class Player(
    val id: Int,
    val name: String,
    val teamName: String,
    val position: String,
    val number: Int,
)

enum class MatchEventType(val label: String) {
    GOAL("Гол"),
    YELLOW_CARD("Жёлтая карточка"),
    RED_CARD("Красная карточка"),
    SUBSTITUTION("Замена"),
}

data class MatchEvent(
    val id: Int,
    val minute: Int,
    val type: MatchEventType,
    val teamName: String,
    val playerName: String,
)

data class Game(
    val id: Int,
    val league: League,
    val homeTeam: Team,
    val awayTeam: Team,
    val date: String,
    val status: GameStatus,
    val homeScore: Int?,
    val awayScore: Int?,
    val homeXg: Double,
    val awayXg: Double,
    val oddsHome: Double,
    val oddsDraw: Double,
    val oddsAway: Double,
    val events: List<MatchEvent> = emptyList(),
)
