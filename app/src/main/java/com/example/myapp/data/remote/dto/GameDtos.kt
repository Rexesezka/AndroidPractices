package com.example.myapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Общий ответ API для списка матчей. */
@Serializable
data class GamesListResponseDto(
    val status: String? = null,
    val count: Int? = null,
    val data: List<GameDto>? = null,
    val message: String? = null,
    val offset: Int? = null,
    @SerialName("TotalCount") val totalCount: Int? = null,
    val traceId: String? = null,
)

/** Матч (сокращённый вариант, /games/list). */
@Serializable
data class GameDto(
    val id: Int,
    val flashId: String? = null,
    val date: String? = null,
    val dateUtc: Long? = null,
    val status: Int? = null,
    val statusName: String? = null,
    val elapsed: Int? = null,
    val homeResult: Int? = null,
    val awayResult: Int? = null,
    val homeHTResult: Int? = null,
    val awayHTResult: Int? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val season: SeasonDto? = null,
    val roundName: String? = null,
    val odds: List<BetDto>? = null,
)

@Serializable
data class TeamDto(
    val id: Int? = null,
    val name: String? = null,
    val flashId: String? = null,
    val logoUrl: String? = null,
    val country: CountryDto? = null,
)

@Serializable
data class CountryDto(
    val code: String? = null,
    val name: String? = null,
)

@Serializable
data class LeagueDto(
    val id: Int? = null,
    val name: String? = null,
    val country: CountryDto? = null,
    val flashScoreId: String? = null,
)

@Serializable
data class SeasonDto(
    val uid: String? = null,
    val year: Int? = null,
    val league: LeagueDto? = null,
)

@Serializable
data class BetDto(
    val marketId: Int? = null,
    val marketName: String? = null,
    val odds: List<PriceDto>? = null,
)

@Serializable
data class PriceDto(
    val name: String? = null,
    val value: Double? = null,
    val openingValue: Double? = null,
)
