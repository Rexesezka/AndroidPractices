package com.example.myapp.data.mapper

import com.example.myapp.core.DateTimeFormat
import com.example.myapp.data.remote.dto.BetDto
import com.example.myapp.data.remote.dto.CountryDto
import com.example.myapp.data.remote.dto.EventDto
import com.example.myapp.data.remote.dto.GameDto
import com.example.myapp.data.remote.dto.GameFullDto
import com.example.myapp.data.remote.dto.GlickoDto
import com.example.myapp.data.remote.dto.LeagueDto
import com.example.myapp.data.remote.dto.StatisticsDto
import com.example.myapp.data.remote.dto.TeamDto
import com.example.myapp.data.remote.dto.VenueDto
import com.example.myapp.domain.model.Country
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.GameDetails
import com.example.myapp.domain.model.GlickoPrediction
import com.example.myapp.domain.model.League
import com.example.myapp.domain.model.MatchEvent
import com.example.myapp.domain.model.MatchEventType
import com.example.myapp.domain.model.MatchStatus
import com.example.myapp.domain.model.OddsItem
import com.example.myapp.domain.model.OddsMarket
import com.example.myapp.domain.model.StatLine
import com.example.myapp.domain.model.Team
import com.example.myapp.domain.model.Venue

fun CountryDto.toDomain(): Country = Country(code = code.orEmpty(), name = name.orEmpty())

fun TeamDto.toDomain(): Team = Team(
    id = id ?: 0,
    name = name ?: "—",
    logoUrl = logoUrl,
    country = country?.toDomain(),
)

fun LeagueDto.toDomain(): League = League(
    id = id ?: 0,
    name = name ?: "—",
    country = country?.toDomain(),
)

fun BetDto.toDomain(): OddsMarket = OddsMarket(
    marketId = marketId ?: 0,
    marketName = marketName,
    odds = odds.orEmpty().map { OddsItem(name = it.name.orEmpty(), value = it.value ?: 0.0) },
)

fun GameDto.toDomain(): Game = Game(
    id = id,
    leagueId = season?.league?.id,
    leagueName = season?.league?.name,
    seasonYear = season?.year,
    roundName = roundName,
    date = DateTimeFormat.format(date),
    status = mapStatus(status),
    statusName = mapStatusName(statusName, mapStatus(status)),
    elapsed = elapsed,
    homeTeam = (homeTeam ?: TeamDto()).toDomain(),
    awayTeam = (awayTeam ?: TeamDto()).toDomain(),
    homeScore = homeResult,
    awayScore = awayResult,
    homeHalfScore = homeHTResult,
    awayHalfScore = awayHTResult,
    odds = odds.orEmpty().map { it.toDomain() },
)

fun GlickoDto.toDomain(): GlickoPrediction = GlickoPrediction(
    homeRating = homeRating,
    awayRating = awayRating,
    homeXg = homeXg,
    awayXg = awayXg,
    homeWinProbability = homeWinProbability,
    awayWinProbability = awayWinProbability,
)

fun EventDto.toDomain(): MatchEvent = MatchEvent(
    id = id ?: 0,
    elapsed = elapsed ?: 0,
    extra = extra,
    type = mapEventType(type, name),
    name = name.orEmpty(),
    teamId = teamId,
    playerName = player?.name?.takeIf { it.isNotBlank() },
    assistName = assistPlayer?.name?.takeIf { it.isNotBlank() },
)

fun VenueDto.toDomain(): Venue = Venue(name = name, city = city, capacity = capacity)

fun GameFullDto.toDomain(glicko: GlickoPrediction?): GameDetails {
    val gameDto = game ?: error("Ответ не содержит данных матча")
    return GameDetails(
        game = gameDto.toDomain(),
        statistics = statistics?.toStatLines().orEmpty(),
        events = events.orEmpty()
            .map { it.toDomain() }
            .sortedBy { it.elapsed * 100 + (it.extra ?: 0) },
        venue = venue?.toDomain(),
        refereeName = refereeName?.takeIf { it.isNotBlank() },
        glicko = glicko,
    )
}

private fun mapStatus(code: Int?): MatchStatus = when (code) {
    1, 2 -> MatchStatus.NOT_STARTED
    3, 4, 5, 6, 7, 11, 12, 19 -> MatchStatus.LIVE
    8, 9, 10, 17, 18 -> MatchStatus.FINISHED
    14 -> MatchStatus.POSTPONED
    13, 15 -> MatchStatus.CANCELLED
    else -> MatchStatus.UNKNOWN
}

private fun mapStatusName(raw: String?, status: MatchStatus): String {
    val name = raw.orEmpty().lowercase()
    return when {
        name.contains("finished") -> "Завершён"
        name.contains("postponed") -> "Перенесён"
        name.contains("cancel") -> "Отменён"
        name.contains("not started") -> "Не начался"
        name.contains("1st half") -> "1-й тайм"
        name.contains("2nd half") -> "2-й тайм"
        name.contains("half") || name.contains("ht") -> "Перерыв"
        name.contains("live") || name.contains("play") -> "В игре"
        raw.isNullOrBlank() -> status.title
        else -> raw
    }
}

private fun mapEventType(type: Int?, name: String?): MatchEventType {
    val value = name.orEmpty().lowercase()
    return when {
        value.contains("own goal") -> MatchEventType.OWN_GOAL
        value.contains("missed penalty") -> MatchEventType.MISSED_PENALTY
        value.contains("penalty") -> MatchEventType.PENALTY
        value.contains("red") -> MatchEventType.RED_CARD
        value.contains("yellow") -> MatchEventType.YELLOW_CARD
        value.contains("substitution") -> MatchEventType.SUBSTITUTION
        value.contains("goal") -> MatchEventType.GOAL
        value.contains("var") -> MatchEventType.VAR
        type == 1 -> MatchEventType.GOAL
        type == 2 -> MatchEventType.YELLOW_CARD
        type == 3 -> MatchEventType.SUBSTITUTION
        type == 4 -> MatchEventType.VAR
        else -> MatchEventType.OTHER
    }
}

private fun StatisticsDto.toStatLines(): List<StatLine> {
    fun intLine(label: String, home: Int?, away: Int?): StatLine? =
        if (home == null && away == null) null
        else StatLine(label, home?.toString() ?: "0", away?.toString() ?: "0")

    fun xgLine(label: String, home: Double?, away: Double?): StatLine? =
        if (home == null && away == null) null
        else StatLine(label, home?.let { "%.2f".format(it) } ?: "0.00", away?.let { "%.2f".format(it) } ?: "0.00")

    return listOfNotNull(
        intLine("Владение мячом, %", ballPossessionHome, ballPossessionAway),
        intLine("Удары всего", totalShotsHome, totalShotsAway),
        intLine("Удары в створ", shotsOnGoalHome, shotsOnGoalAway),
        intLine("Угловые", cornerKicksHome, cornerKicksAway),
        intLine("Фолы", foulsHome, foulsAway),
        intLine("Жёлтые карточки", yellowCardsHome, yellowCardsAway),
        intLine("Красные карточки", redCardsHome, redCardsAway),
        intLine("Офсайды", offsidesHome, offsidesAway),
        intLine("Сэйвы вратаря", goalkeeperSavesHome, goalkeeperSavesAway),
        xgLine("xG", expectedGoalsHome, expectedGoalsAway),
    )
}
