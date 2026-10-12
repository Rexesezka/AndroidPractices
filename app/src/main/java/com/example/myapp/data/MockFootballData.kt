package com.example.myapp.data

import com.example.myapp.data.model.Game
import com.example.myapp.data.model.GameStatus
import com.example.myapp.data.model.League
import com.example.myapp.data.model.MatchEvent
import com.example.myapp.data.model.MatchEventType
import com.example.myapp.data.model.Player
import com.example.myapp.data.model.Team
import kotlin.random.Random

/**
 * Моковые данные вместо реальных запросов к sstats.net.
 * В рамках практики сеть не используется — только подготовка экранов.
 */
object MockFootballData {

    val leagues: List<League> = listOf(
        League(1, "Английская Премьер-лига", "Англия", "2026/27"),
        League(2, "Ла Лига", "Испания", "2026/27"),
        League(3, "Серия А", "Италия", "2026/27"),
        League(4, "Бундеслига", "Германия", "2026/27"),
        League(5, "Российская Премьер-лига", "Россия", "2026/27"),
    )

    private val leagueClubs: Map<Int, List<String>> = mapOf(
        1 to listOf("Арсенал", "Манчестер Сити", "Ливерпуль", "Челси", "Тоттенхэм", "Ньюкасл"),
        2 to listOf("Реал Мадрид", "Барселона", "Атлетико", "Севилья", "Валенсия", "Бетис"),
        3 to listOf("Ювентус", "Интер", "Милан", "Наполи", "Рома", "Лацио"),
        4 to listOf("Бавария", "Боруссия Дортмунд", "РБ Лейпциг", "Байер", "Штутгарт", "Вольфсбург"),
        5 to listOf("Зенит", "Спартак", "ЦСКА", "Локомотив", "Краснодар", "Динамо"),
    )

    val teams: List<Team> = leagues.flatMap { league ->
        leagueClubs.getValue(league.id).mapIndexed { index, clubName ->
            Team(
                id = league.id * 100 + index + 1,
                name = clubName,
                country = league.country,
            )
        }
    }

    val players: List<Player> = teams.flatMapIndexed { teamIndex, team ->
        listOf("Вратарь", "Защитник", "Полузащитник", "Нападающий").mapIndexed { index, position ->
            Player(
                id = teamIndex * 10 + index + 1,
                name = "${team.name} #${index + 1}",
                teamName = team.name,
                position = position,
                number = index + 1,
            )
        }
    }

    val games: List<Game> = createGames()

    private fun createGames(): List<Game> {
        val random = Random(seed = 42)
        return List(57) { index ->
            val league = leagues[random.nextInt(leagues.size)]
            val clubs = teams.filter { it.country == league.country }
            val home = clubs[random.nextInt(clubs.size)]
            val away = clubs.filter { it.id != home.id }[random.nextInt(clubs.size - 1)]

            val status = when {
                index % 7 == 0 -> GameStatus.SCHEDULED
                index % 11 == 0 -> GameStatus.LIVE
                else -> GameStatus.FINISHED
            }
            val hasScore = status != GameStatus.SCHEDULED

            Game(
                id = index + 1,
                league = league,
                homeTeam = home,
                awayTeam = away,
                date = "%02d.10.2026 %02d:%s".format(1 + index % 28, 12 + index % 9, if (index % 2 == 0) "00" else "30"),
                status = status,
                homeScore = if (hasScore) random.nextInt(0, 5) else null,
                awayScore = if (hasScore) random.nextInt(0, 5) else null,
                homeXg = 0.5 + random.nextDouble() * 2.5,
                awayXg = 0.5 + random.nextDouble() * 2.5,
                oddsHome = 1.2 + random.nextDouble() * 3.0,
                oddsDraw = 2.8 + random.nextDouble() * 2.0,
                oddsAway = 1.3 + random.nextDouble() * 3.5,
                events = if (hasScore) createEvents(home.name, away.name, random) else emptyList(),
            )
        }
    }

    private fun createEvents(homeName: String, awayName: String, random: Random): List<MatchEvent> {
        val count = random.nextInt(2, 6)
        return List(count) { i ->
            val teamName = if (random.nextBoolean()) homeName else awayName
            MatchEvent(
                id = i + 1,
                minute = random.nextInt(1, 90),
                type = MatchEventType.entries[random.nextInt(MatchEventType.entries.size)],
                teamName = teamName,
                playerName = "$teamName #${random.nextInt(1, 12)}",
            )
        }.sortedBy { it.minute }
    }
}
