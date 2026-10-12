package com.example.myapp.domain.model

/** Тип события матча. */
enum class MatchEventType(val title: String) {
    GOAL("Гол"),
    OWN_GOAL("Автогол"),
    PENALTY("Пенальти"),
    MISSED_PENALTY("Незабитый пенальти"),
    YELLOW_CARD("Жёлтая карточка"),
    RED_CARD("Красная карточка"),
    SUBSTITUTION("Замена"),
    VAR("VAR"),
    OTHER("Событие"),
}

/** Событие матча (гол, карточка, замена и т.д.). */
data class MatchEvent(
    val id: Int,
    val elapsed: Int,
    val extra: Int?,
    val type: MatchEventType,
    val name: String,
    val teamId: Int?,
    val playerName: String?,
    val assistName: String?,
) {
    val minuteLabel: String
        get() = if (extra != null && extra > 0) "$elapsed+$extra'" else "$elapsed'"
}

/** Одна строка статистики (значение хозяев и гостей). */
data class StatLine(
    val label: String,
    val home: String,
    val away: String,
)

/** Стадион. */
data class Venue(
    val name: String?,
    val city: String?,
    val capacity: Int?,
)

/** Прогноз Glicko 2 и xG (ответ /games/glicko/{id}). */
data class GlickoPrediction(
    val homeRating: Double?,
    val awayRating: Double?,
    val homeXg: Double?,
    val awayXg: Double?,
    val homeWinProbability: Double?,
    val awayWinProbability: Double?,
)

/**
 * Полные данные матча (экран деталей). Объединяет ответы
 * /games/{id} и /games/glicko/{id}.
 */
data class GameDetails(
    val game: Game,
    val statistics: List<StatLine>,
    val events: List<MatchEvent>,
    val venue: Venue?,
    val refereeName: String?,
    val glicko: GlickoPrediction?,
)
