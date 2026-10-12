package com.example.myapp.data.remote.dto

import kotlinx.serialization.Serializable

/** Ответ /games/glicko/{id}. */
@Serializable
data class GlickoResponseDto(
    val status: String? = null,
    val data: GlickoDataDto? = null,
    val message: String? = null,
)

@Serializable
data class GlickoDataDto(
    val fixture: GameDto? = null,
    val glicko: GlickoDto? = null,
)

@Serializable
data class GlickoDto(
    val homeRating: Double? = null,
    val homeRd: Double? = null,
    val awayRating: Double? = null,
    val awayRd: Double? = null,
    val homeXg: Double? = null,
    val awayXg: Double? = null,
    val homeWinProbability: Double? = null,
    val awayWinProbability: Double? = null,
    val homeVolatility: Double? = null,
    val awayVolatility: Double? = null,
)
