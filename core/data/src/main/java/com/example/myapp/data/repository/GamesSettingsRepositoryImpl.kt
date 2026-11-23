package com.example.myapp.data.repository

import com.example.myapp.data.local.preferences.GamesSettingsDataStore
import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.repository.GamesSettingsRepository
import kotlinx.coroutines.flow.Flow

/** Реализация хранилища настроек фильтра поверх DataStore. */
class GamesSettingsRepositoryImpl(
    private val dataStore: GamesSettingsDataStore,
) : GamesSettingsRepository {

    override fun observeSettings(): Flow<GamesQuery> = dataStore.settings

    override suspend fun saveSettings(query: GamesQuery) = dataStore.save(query)
}
