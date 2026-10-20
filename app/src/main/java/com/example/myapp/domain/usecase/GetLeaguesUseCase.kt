package com.example.myapp.domain.usecase

import com.example.myapp.core.NetworkResult
import com.example.myapp.domain.model.League
import com.example.myapp.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow

/** Получение списка лиг. */
class GetLeaguesUseCase(
    private val repository: FootballRepository,
) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<NetworkResult<List<League>>> =
        repository.getLeagues(forceRefresh)
}
