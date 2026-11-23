package com.example.myapp.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Предустановленные фильтры списка матчей (query-параметры /games/list). */
enum class GamesFilter(val title: String) {
    ALL("Все"),
    TODAY("Сегодня"),
    LIVE("Лайв"),
    ENDED("Завершённые"),
    UPCOMING("Предстоящие"),
}

/**
 * Параметры запроса списка матчей. Позволяет настраивать запрос
 * (фильтр, лига, год) — требование "настраиваемый запрос с query-параметрами".
 */
data class GamesQuery(
    val filter: GamesFilter = GamesFilter.ALL,
    val leagueId: Int? = null,
    val year: Int? = null,
) {

    fun toQueryParams(offset: Int, limit: Int): Map<String, String> = buildMap {
        put("offset", offset.toString())
        put("limit", limit.toString())
        when (filter) {
            GamesFilter.ALL -> {
                put("from", dateOffsetDays(-7))
                put("to", dateOffsetDays(7))
                put("order", "-1")
            }
            GamesFilter.TODAY -> {
                put("today", "true")
                put("order", "-1")
            }
            GamesFilter.LIVE -> {
                put("live", "true")
                put("order", "-1")
            }
            GamesFilter.ENDED -> {
                put("ended", "true")
                put("order", "-1")
            }
            GamesFilter.UPCOMING -> {
                put("upcoming", "true")
                put("order", "1")
            }
        }
        leagueId?.let { put("leagueid", it.toString()) }
        year?.let { put("year", it.toString()) }
    }

    fun cacheKey(): String = "${filter.name}:${leagueId ?: "-"}:${year ?: "-"}"

    /** Настройки не отличаются от значений по умолчанию (страница в дефолтном состоянии). */
    val isDefault: Boolean
        get() = filter == GamesFilter.ALL && leagueId == null && year == null

    private fun dateOffsetDays(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }
}
