package com.example.myapp.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapp.domain.model.GamesFilter
import com.example.myapp.domain.model.GamesQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Единственный экземпляр DataStore для настроек фильтра. */
private val Context.gamesSettingsStore: DataStore<Preferences> by preferencesDataStore(
    name = "games_settings",
)

/**
 * Хранение настроек фильтра в DataStore Preferences.
 * Настройки сохраняются между запусками приложения.
 */
class GamesSettingsDataStore(
    private val context: Context,
) {

    private object Keys {
        val FILTER = stringPreferencesKey("filter")
        val LEAGUE_ID = intPreferencesKey("league_id")
        val YEAR = intPreferencesKey("year")
    }

    val settings: Flow<GamesQuery> = context.gamesSettingsStore.data
        .catch { throwable ->
            // Повреждённый файл настроек не должен ронять экран — отдаём значения по умолчанию.
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { preferences ->
            GamesQuery(
                filter = preferences[Keys.FILTER]
                    ?.let { name -> GamesFilter.entries.firstOrNull { it.name == name } }
                    ?: GamesFilter.ALL,
                leagueId = preferences[Keys.LEAGUE_ID],
                year = preferences[Keys.YEAR],
            )
        }

    suspend fun save(query: GamesQuery) {
        context.gamesSettingsStore.edit { preferences ->
            preferences[Keys.FILTER] = query.filter.name
            val leagueId = query.leagueId
            val year = query.year
            if (leagueId == null) {
                preferences.remove(Keys.LEAGUE_ID)
            } else {
                preferences[Keys.LEAGUE_ID] = leagueId
            }
            if (year == null) {
                preferences.remove(Keys.YEAR)
            } else {
                preferences[Keys.YEAR] = year
            }
        }
    }
}
