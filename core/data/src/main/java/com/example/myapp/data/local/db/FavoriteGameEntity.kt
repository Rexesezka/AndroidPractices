package com.example.myapp.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Избранный матч в локальной БД. Храним только то, что нужно для
 * отображения списка избранного без интернета.
 */
@Entity(tableName = "favorite_games")
data class FavoriteGameEntity(
    @PrimaryKey val id: Int,
    val leagueName: String?,
    val seasonYear: Int?,
    val date: String?,
    val statusTitle: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val savedAt: Long,
)
