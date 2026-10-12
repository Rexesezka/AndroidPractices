package com.example.myapp.domain.usecase

import com.example.myapp.core.NetworkResult
import com.example.myapp.domain.model.GameDetails
import com.example.myapp.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow

/** Получение полных данных матча по id. */
class GetGameDetailsUseCase(
    private val repository: FootballRepository,
) {
    operator fun invoke(gameId: Int): Flow<NetworkResult<GameDetails>> =
        repository.getGameDetails(gameId)
}
