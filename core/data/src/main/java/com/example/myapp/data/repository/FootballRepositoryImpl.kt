package com.example.myapp.data.repository

import com.example.myapp.core.InMemoryCache
import com.example.myapp.core.NetworkResult
import com.example.myapp.data.mapper.toDomain
import com.example.myapp.data.remote.SstatsApi
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.GameDetails
import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.model.League
import com.example.myapp.domain.repository.FootballRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Реализация репозитория: обращается к sstats.net через Retrofit.
 *
 * Все сетевые операции выполняются на [ioDispatcher] (Dispatchers.IO).
 * Результаты кэшируются в памяти (кэш с TTL) — бонусное задание по Flow.
 */
class FootballRepositoryImpl(
    private val api: SstatsApi,
    private val gamesCache: InMemoryCache<List<Game>>,
    private val leaguesCache: InMemoryCache<List<League>>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FootballRepository {

    override fun getGamesPage(
        query: GamesQuery,
        offset: Int,
        limit: Int,
        forceRefresh: Boolean,
    ): Flow<NetworkResult<List<Game>>> = flow {
        val cacheKey = "games:${query.cacheKey()}:$offset:$limit"
        val cached = gamesCache.get(cacheKey)

        if (cached != null && !forceRefresh) {
            emit(NetworkResult.Success(cached, fromCache = true))
            return@flow
        }
        if (cached == null) {
            emit(NetworkResult.Loading)
        }

        val response = api.getGames(query.toQueryParams(offset, limit))
        val games = response.data.orEmpty().map { it.toDomain() }
        gamesCache.put(cacheKey, games)
        emit(NetworkResult.Success(games, fromCache = false))
    }.catch { throwable ->
        emit(NetworkResult.Error(throwable))
    }.flowOn(ioDispatcher)

    override fun getGameDetails(gameId: Int): Flow<NetworkResult<GameDetails>> = flow {
        emit(NetworkResult.Loading)

        val full = api.getGameDetails(gameId).data
            ?: error("Матч не найден")

        // Прогноз Glicko 2 не критичен: при ошибке просто не показываем блок.
        val glicko = runCatching { api.getGlicko(gameId).data?.glicko?.toDomain() }.getOrNull()

        emit(NetworkResult.Success(full.toDomain(glicko)))
    }.catch { throwable ->
        emit(NetworkResult.Error(throwable))
    }.flowOn(ioDispatcher)

    override fun getLeagues(forceRefresh: Boolean): Flow<NetworkResult<List<League>>> = flow {
        val cacheKey = "leagues"
        val cached = leaguesCache.get(cacheKey)

        if (cached != null && !forceRefresh) {
            emit(NetworkResult.Success(cached, fromCache = true))
            return@flow
        }
        if (cached == null) {
            emit(NetworkResult.Loading)
        }

        val leagues = api.getLeagues().data.orEmpty().map { it.toDomain() }
        leaguesCache.put(cacheKey, leagues)
        emit(NetworkResult.Success(leagues, fromCache = false))
    }.catch { throwable ->
        emit(NetworkResult.Error(throwable))
    }.flowOn(ioDispatcher)
}
