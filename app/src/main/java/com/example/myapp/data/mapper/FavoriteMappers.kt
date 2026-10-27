package com.example.myapp.data.mapper

import com.example.myapp.data.local.db.FavoriteGameEntity
import com.example.myapp.domain.model.FavoriteGame
import com.example.myapp.domain.model.Game

fun Game.toFavoriteEntity(savedAt: Long): FavoriteGameEntity = FavoriteGameEntity(
    id = id,
    leagueName = leagueName,
    seasonYear = seasonYear,
    date = date,
    statusTitle = statusName ?: status.title,
    homeTeamName = homeTeam.name,
    awayTeamName = awayTeam.name,
    homeScore = homeScore,
    awayScore = awayScore,
    savedAt = savedAt,
)

fun FavoriteGameEntity.toDomain(): FavoriteGame = FavoriteGame(
    id = id,
    leagueName = leagueName,
    seasonYear = seasonYear,
    date = date,
    statusTitle = statusTitle,
    homeTeamName = homeTeamName,
    awayTeamName = awayTeamName,
    homeScore = homeScore,
    awayScore = awayScore,
)
