package com.example.myapp.domain.model

/** Страна. */
data class Country(
    val code: String,
    val name: String,
)

/** Команда. */
data class Team(
    val id: Int,
    val name: String,
    val logoUrl: String? = null,
    val country: Country? = null,
)

/** Лига/турнир. */
data class League(
    val id: Int,
    val name: String,
    val country: Country? = null,
)

/** Один коэффициент (например, Home = 1.95). */
data class OddsItem(
    val name: String,
    val value: Double,
)

/** Рынок коэффициентов (например, "Match Winner"). */
data class OddsMarket(
    val marketId: Int,
    val marketName: String?,
    val odds: List<OddsItem>,
)

/** Группа статусов матча. */
enum class MatchStatus(val title: String) {
    NOT_STARTED("Не начался"),
    LIVE("В игре"),
    FINISHED("Завершён"),
    POSTPONED("Перенесён"),
    CANCELLED("Отменён"),
    UNKNOWN("Неизвестно"),
}

/**
 * Матч (элемент списка). Соответствует ответу /games/list.
 */
data class Game(
    val id: Int,
    val leagueId: Int?,
    val leagueName: String?,
    val seasonYear: Int?,
    val roundName: String?,
    val date: String?,
    val status: MatchStatus,
    val statusName: String?,
    val elapsed: Int?,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val homeHalfScore: Int?,
    val awayHalfScore: Int?,
    val odds: List<OddsMarket>,
)
