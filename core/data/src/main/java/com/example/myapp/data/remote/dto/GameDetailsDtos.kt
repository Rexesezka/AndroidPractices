package com.example.myapp.data.remote.dto

import kotlinx.serialization.Serializable

/** Ответ /games/{id}. */
@Serializable
data class GameDetailsResponseDto(
    val status: String? = null,
    val data: GameFullDto? = null,
    val message: String? = null,
)

@Serializable
data class GameFullDto(
    val game: GameDto? = null,
    val statistics: StatisticsDto? = null,
    val events: List<EventDto>? = null,
    val venue: VenueDto? = null,
    val refereeName: String? = null,
)

/** Статистика матча (используется только часть полей). */
@Serializable
data class StatisticsDto(
    val ballPossessionHome: Int? = null,
    val ballPossessionAway: Int? = null,
    val totalShotsHome: Int? = null,
    val totalShotsAway: Int? = null,
    val shotsOnGoalHome: Int? = null,
    val shotsOnGoalAway: Int? = null,
    val cornerKicksHome: Int? = null,
    val cornerKicksAway: Int? = null,
    val foulsHome: Int? = null,
    val foulsAway: Int? = null,
    val yellowCardsHome: Int? = null,
    val yellowCardsAway: Int? = null,
    val redCardsHome: Int? = null,
    val redCardsAway: Int? = null,
    val offsidesHome: Int? = null,
    val offsidesAway: Int? = null,
    val goalkeeperSavesHome: Int? = null,
    val goalkeeperSavesAway: Int? = null,
    val expectedGoalsHome: Double? = null,
    val expectedGoalsAway: Double? = null,
)

@Serializable
data class EventDto(
    val id: Int? = null,
    val teamId: Int? = null,
    val elapsed: Int? = null,
    val extra: Int? = null,
    val type: Int? = null,
    val name: String? = null,
    val player: PlayerDto? = null,
    val assistPlayer: PlayerDto? = null,
)

@Serializable
data class PlayerDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class VenueDto(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
)
