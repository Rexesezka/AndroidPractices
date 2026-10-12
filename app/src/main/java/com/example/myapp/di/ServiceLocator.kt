package com.example.myapp.di

import com.example.myapp.BuildConfig
import com.example.myapp.core.InMemoryCache
import com.example.myapp.data.remote.SstatsApi
import com.example.myapp.data.repository.FootballRepositoryImpl
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.League
import com.example.myapp.domain.repository.FootballRepository
import com.example.myapp.domain.usecase.GetGameDetailsUseCase
import com.example.myapp.domain.usecase.GetGamesPageUseCase
import com.example.myapp.domain.usecase.GetLeaguesUseCase
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Простой контейнер зависимостей (manual DI).
 * Собирает сетевой стек, кэш, репозиторий и UseCase-ы.
 */
object ServiceLocator {

    private const val BASE_URL = "https://api.sstats.net/"

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
}
