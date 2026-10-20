package com.example.myapp.data.remote.dto

import kotlinx.serialization.Serializable

/** Ответ /leagues (сезоны не используются, поэтому не объявляются). */
@Serializable
data class LeaguesResponseDto(
    val status: String? = null,
    val data: List<LeagueDto>? = null,
    val message: String? = null,
)
