package com.example.myapp.domain.repository

import com.example.myapp.domain.model.FavoriteGame
import com.example.myapp.domain.model.Game
import kotlinx.coroutines.flow.Flow

/**
 * Локальное хранилище избранных матчей (Room).
 * Не зависит от сети — избранное доступно офлайн.
 */
interface FavoritesRepository {

    /** Все избранные матчи (свежие сверху). */
    fun observeFavorites(): Flow<List<FavoriteGame>>

    /** Идентификаторы избранных матчей — для отметок в списке. */
    fun observeFavoriteIds(): Flow<Set<Int>>

    /** Наблюдение за тем, добавлен ли конкретный матч в избранное. */
    fun observeIsFavorite(gameId: Int): Flow<Boolean>

    /** Добавить матч в избранное или убрать из него. */
    suspend fun toggleFavorite(game: Game)

    /** Убрать матч из избранного. */
    suspend fun removeFavorite(gameId: Int)
}
