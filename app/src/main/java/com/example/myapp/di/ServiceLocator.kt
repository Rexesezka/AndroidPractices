package com.example.myapp.di

import android.content.Context
import com.example.myapp.BuildConfig
import com.example.myapp.core.FilterBadgeCache
import com.example.myapp.core.InMemoryCache
import com.example.myapp.data.local.db.AppDatabase
import com.example.myapp.data.local.preferences.GamesSettingsDataStore
import com.example.myapp.data.remote.SstatsApi
import com.example.myapp.data.repository.FavoritesRepositoryImpl
import com.example.myapp.data.repository.FootballRepositoryImpl
import com.example.myapp.data.repository.GamesSettingsRepositoryImpl
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.League
import com.example.myapp.domain.repository.FavoritesRepository
import com.example.myapp.domain.repository.FootballRepository
import com.example.myapp.domain.repository.GamesSettingsRepository
import com.example.myapp.domain.usecase.GetGameDetailsUseCase
import com.example.myapp.domain.usecase.GetGamesPageUseCase
import com.example.myapp.domain.usecase.GetLeaguesUseCase
import com.example.myapp.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.myapp.domain.usecase.ObserveFavoritesUseCase
import com.example.myapp.domain.usecase.ObserveGamesSettingsUseCase
import com.example.myapp.domain.usecase.ObserveIsFavoriteUseCase
import com.example.myapp.domain.usecase.RemoveFavoriteUseCase
import com.example.myapp.domain.usecase.SaveGamesSettingsUseCase
import com.example.myapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Простой контейнер зависимостей (manual DI).
 * Собирает сетевой стек, локальное хранилище (DataStore, Room), кэш,
 * репозитории и UseCase-ы.
 */
object ServiceLocator {

    private const val BASE_URL = "https://api.sstats.net/"

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val api: SstatsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SstatsApi::class.java)
    }

    private val gamesCache = InMemoryCache<List<Game>>(ttlMillis = 60_000L)
    private val leaguesCache = InMemoryCache<List<League>>(ttlMillis = 300_000L)

    val repository: FootballRepository by lazy {
        FootballRepositoryImpl(
            api = api,
            gamesCache = gamesCache,
            leaguesCache = leaguesCache,
        )
    }

    val getGamesPageUseCase: GetGamesPageUseCase by lazy { GetGamesPageUseCase(repository) }
    val getGameDetailsUseCase: GetGameDetailsUseCase by lazy { GetGameDetailsUseCase(repository) }
    val getLeaguesUseCase: GetLeaguesUseCase by lazy { GetLeaguesUseCase(repository) }

    // --- Локальное хранение: настройки фильтра (DataStore) ---

    private val gamesSettingsDataStore: GamesSettingsDataStore by lazy {
        GamesSettingsDataStore(appContext)
    }

    private val gamesSettingsRepository: GamesSettingsRepository by lazy {
        GamesSettingsRepositoryImpl(gamesSettingsDataStore)
    }

    val observeGamesSettingsUseCase: ObserveGamesSettingsUseCase by lazy {
        ObserveGamesSettingsUseCase(gamesSettingsRepository)
    }

    val saveGamesSettingsUseCase: SaveGamesSettingsUseCase by lazy {
        SaveGamesSettingsUseCase(gamesSettingsRepository)
    }

    // --- Локальное хранение: избранное (Room) ---

    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    private val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepositoryImpl(database.favoriteGameDao())
    }

    val observeFavoritesUseCase: ObserveFavoritesUseCase by lazy {
        ObserveFavoritesUseCase(favoritesRepository)
    }

    val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase by lazy {
        ObserveFavoriteIdsUseCase(favoritesRepository)
    }

    val observeIsFavoriteUseCase: ObserveIsFavoriteUseCase by lazy {
        ObserveIsFavoriteUseCase(favoritesRepository)
    }

    val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(favoritesRepository)
    }

    val removeFavoriteUseCase: RemoveFavoriteUseCase by lazy {
        RemoveFavoriteUseCase(favoritesRepository)
    }

    // --- Общий кэш бейджа (создаётся один раз через DI) ---

    val filterBadgeCache: FilterBadgeCache by lazy { FilterBadgeCache() }
}
