package com.example.myapp.domain.repository

import com.example.myapp.domain.model.GamesQuery
import kotlinx.coroutines.flow.Flow

/**
 * Хранилище настроек фильтра списка матчей.
 * Реализация — DataStore (настройки переживают перезапуск приложения).
 */
interface GamesSettingsRepository {

    /** Текущие сохранённые настройки. */
    fun observeSettings(): Flow<GamesQuery>

    /** Сохранить настройки. */
    suspend fun saveSettings(query: GamesQuery)
}
