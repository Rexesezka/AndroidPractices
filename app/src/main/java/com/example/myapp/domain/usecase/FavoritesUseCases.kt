package com.example.myapp.domain.usecase

import com.example.myapp.domain.model.FavoriteGame
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/** Список избранных матчей. */
class ObserveFavoritesUseCase(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(): Flow<List<FavoriteGame>> = repository.observeFavorites()
}

/** Идентификаторы избранных матчей для отметок в списке. */
class ObserveFavoriteIdsUseCase(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = repository.observeFavoriteIds()
}

/** Добавлен ли матч в избранное. */
class ObserveIsFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(gameId: Int): Flow<Boolean> = repository.observeIsFavorite(gameId)
}

/** Добавление/удаление матча из избранного. */
class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(game: Game) = repository.toggleFavorite(game)
}

/** Удаление матча из избранного. */
class RemoveFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(gameId: Int) = repository.removeFavorite(gameId)
}
