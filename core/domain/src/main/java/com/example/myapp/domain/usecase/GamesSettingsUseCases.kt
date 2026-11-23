package com.example.myapp.domain.usecase

import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.repository.GamesSettingsRepository
import kotlinx.coroutines.flow.Flow

/** Наблюдение за сохранёнными настройками фильтра. */
class ObserveGamesSettingsUseCase(
    private val repository: GamesSettingsRepository,
) {
    operator fun invoke(): Flow<GamesQuery> = repository.observeSettings()
}

/** Сохранение настроек фильтра. */
class SaveGamesSettingsUseCase(
    private val repository: GamesSettingsRepository,
) {
    suspend operator fun invoke(query: GamesQuery) = repository.saveSettings(query)
}
