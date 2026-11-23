package com.example.myapp.data.repository

import com.example.myapp.data.local.db.FavoriteGameDao
import com.example.myapp.data.mapper.toDomain
import com.example.myapp.data.mapper.toFavoriteEntity
import com.example.myapp.domain.model.FavoriteGame
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Реализация избранного поверх Room. Работает полностью офлайн. */
class FavoritesRepositoryImpl(
    private val dao: FavoriteGameDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<FavoriteGame>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        dao.observeIds().map { it.toSet() }

    override fun observeIsFavorite(gameId: Int): Flow<Boolean> = dao.observeIsFavorite(gameId)

    override suspend fun toggleFavorite(game: Game) = withContext(ioDispatcher) {
        if (dao.isFavorite(game.id)) {
            dao.deleteById(game.id)
        } else {
            dao.insert(game.toFavoriteEntity(savedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun removeFavorite(gameId: Int) = withContext(ioDispatcher) {
        dao.deleteById(gameId)
    }
}
