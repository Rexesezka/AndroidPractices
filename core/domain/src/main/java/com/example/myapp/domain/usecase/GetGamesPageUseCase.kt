package com.example.myapp.domain.usecase

import com.example.myapp.core.NetworkResult
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow

/** Получение страницы матчей с учётом фильтров и пагинации. */
class GetGamesPageUseCase(
    private val repository: FootballRepository,
) {
    operator fun invoke(
        query: GamesQuery,
        offset: Int,
        limit: Int,
        forceRefresh: Boolean = false,
    ): Flow<NetworkResult<List<Game>>> =
        repository.getGamesPage(query, offset, limit, forceRefresh)
}
