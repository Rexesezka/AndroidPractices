package com.example.myapp.ui.navigation

/**
 * Маршруты навигации приложения.
 */
object Destinations {

    const val GAMES = "games"
    const val GAME_DETAILS = "games/{gameId}"

    const val TEAMS = "teams"
    const val PLAYERS = "players"
    const val LEAGUES = "leagues"

    const val ARG_GAME_ID = "gameId"

    fun gameDetails(gameId: Int): String = "games/$gameId"
}
