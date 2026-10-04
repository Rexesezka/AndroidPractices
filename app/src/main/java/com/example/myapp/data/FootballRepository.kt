package com.example.myapp.data

import com.example.myapp.data.model.Game
import kotlinx.coroutines.delay

/**
 * Источник данных футбольной статистики.
 * Пагинация соответствует схеме API offset/limit (/games/list).
 */
interface FootballRepository {

    suspend fun getGames(offset: Int, limit: Int): List<Game>

    suspend fun getGameById(id: Int): Game?
}

/**
 * Моковая реализация: имитирует задержку сети и постраничную выдачу.
 */
object MockFootballRepository : FootballRepository {

    private const val NETWORK_DELAY_MS = 700L

    override suspend fun getGames(offset: Int, limit: Int): List<Game> {
        delay(NETWORK_DELAY_MS)
        return MockFootballData.games.drop(offset).take(limit)
    }

    override suspend fun getGameById(id: Int): Game? {
        delay(500L)
        return MockFootballData.games.firstOrNull { it.id == id }
    }
}
