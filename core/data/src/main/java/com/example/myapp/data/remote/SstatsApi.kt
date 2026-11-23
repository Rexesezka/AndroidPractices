package com.example.myapp.data.remote

import com.example.myapp.data.remote.dto.GameDetailsResponseDto
import com.example.myapp.data.remote.dto.GamesListResponseDto
import com.example.myapp.data.remote.dto.GlickoResponseDto
import com.example.myapp.data.remote.dto.LeaguesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * REST API sstats.net (https://api.sstats.net).
 *
 * Запросы настраиваются query-параметрами (@QueryMap) и path-параметрами (@Path).
 */
interface SstatsApi {

    /** Список матчей с фильтрами и пагинацией: /games/list?offset=&limit=&today=true... */
    @GET("games/list")
    suspend fun getGames(@QueryMap params: Map<String, String>): GamesListResponseDto

    /** Полные данные матча: /games/{id}. */
    @GET("games/{id}")
    suspend fun getGameDetails(@Path("id") id: Int): GameDetailsResponseDto

    /** Прогноз Glicko 2 и xG: /games/glicko/{id}. */
    @GET("games/glicko/{id}")
    suspend fun getGlicko(@Path("id") id: Int): GlickoResponseDto

    /** Список лиг: /leagues. */
    @GET("leagues")
    suspend fun getLeagues(): LeaguesResponseDto
}
