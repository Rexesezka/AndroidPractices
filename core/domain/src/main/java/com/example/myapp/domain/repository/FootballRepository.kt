package com.example.myapp.domain.repository

import com.example.myapp.core.NetworkResult
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.GameDetails
import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.model.League
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий футбольной статистики (доменный интерфейс).
 * Слой данных (data) реализует его, обращаясь к REST API.
 */
interface FootballRepository {

    /** Страница списка матчей с учётом фильтров и пагинации (offset/limit). */
    fun getGamesPage(
        query: GamesQuery,
        offset: Int,
        limit: Int,
        forceRefresh: Boolean = false,
    ): Flow<NetworkResult<List<Game>>>

    /** Полные данные матча по id (path-параметр /games/{id}). */
    fun getGameDetails(gameId: Int): Flow<NetworkResult<GameDetails>>

    /** Список лиг. */
    fun getLeagues(forceRefresh: Boolean = false): Flow<NetworkResult<List<League>>>
}
