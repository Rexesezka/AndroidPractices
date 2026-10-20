package com.example.myapp.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.core.FilterBadgeCache
import com.example.myapp.di.ServiceLocator
import com.example.myapp.domain.model.GamesFilter
import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.usecase.ObserveGamesSettingsUseCase
import com.example.myapp.domain.usecase.SaveGamesSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GamesFilterUiState(
    val filter: GamesFilter = GamesFilter.ALL,
    val leagueId: String = "",
    val year: String = "",
    val isLoading: Boolean = true,
)

/**
 * ViewModel экрана настроек фильтра. Загружает текущие настройки из DataStore,
 * а по нажатию «Готово» сохраняет их и обновляет общий кэш бейджа.
 */
class GamesFilterViewModel(
    observeGamesSettings: ObserveGamesSettingsUseCase,
    private val saveGamesSettings: SaveGamesSettingsUseCase,
    private val filterBadgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesFilterUiState())
    val uiState: StateFlow<GamesFilterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val query = observeGamesSettings().first()
            _uiState.update {
                it.copy(
                    filter = query.filter,
                    leagueId = query.leagueId?.toString().orEmpty(),
                    year = query.year?.toString().orEmpty(),
                    isLoading = false,
                )
            }
        }
    }

    fun selectFilter(filter: GamesFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun updateLeagueId(value: String) {
        _uiState.update { it.copy(leagueId = value.filter(Char::isDigit)) }
    }

    fun updateYear(value: String) {
        _uiState.update { it.copy(year = value.filter(Char::isDigit).take(4)) }
    }

    /** Сбросить поля формы к значениям по умолчанию (сохранится по «Готово»). */
    fun reset() {
        _uiState.update { it.copy(filter = GamesFilter.ALL, leagueId = "", year = "") }
    }

    /** Сохранить настройки в DataStore; [onSaved] вызывается после записи. */
    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val query = GamesQuery(
            filter = state.filter,
            leagueId = state.leagueId.toIntOrNull(),
            year = state.year.toIntOrNull(),
        )
        viewModelScope.launch {
            saveGamesSettings(query)
            filterBadgeCache.update(!query.isDefault)
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GamesFilterViewModel(
                    observeGamesSettings = ServiceLocator.observeGamesSettingsUseCase,
                    saveGamesSettings = ServiceLocator.saveGamesSettingsUseCase,
                    filterBadgeCache = ServiceLocator.filterBadgeCache,
                )
            }
        }
    }
}
