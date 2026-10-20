package com.example.myapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.core.ErrorMapper
import com.example.myapp.core.NetworkResult
import com.example.myapp.di.ServiceLocator
import com.example.myapp.domain.model.Game
import com.example.myapp.domain.model.GamesFilter
import com.example.myapp.domain.model.GamesQuery
import com.example.myapp.domain.usecase.GetGamesPageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameListUiState(
    val query: GamesQuery = GamesQuery(),
    val games: List<Game> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isEndReached: Boolean = false,
    val isFromCache: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel списка матчей. Управляет сетевыми запросами: фильтры (query-параметры),
 * постраничная загрузка (offset/limit), состояния загрузки и ошибок.
 */
class GameListViewModel(
    private val getGamesPage: GetGamesPageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameListUiState())
    val uiState: StateFlow<GameListUiState> = _uiState.asStateFlow()

    private var nextOffset = 0
    private var loadJob: Job? = null

    init {
        loadPage(reset = true)
    }

    fun selectFilter(filter: GamesFilter) {
        if (filter == _uiState.value.query.filter) return
        _uiState.update {
            it.copy(query = it.query.copy(filter = filter), games = emptyList(), isEndReached = false)
        }
        loadPage(reset = true)
    }

    fun applyAdvancedFilter(leagueId: Int?, year: Int?) {
        _uiState.update {
            it.copy(query = it.query.copy(leagueId = leagueId, year = year), games = emptyList(), isEndReached = false)
        }
        loadPage(reset = true)
    }

    fun refresh() {
        _uiState.update { it.copy(games = emptyList(), isEndReached = false) }
        loadPage(reset = true, forceRefresh = true)
    }

    fun retry() {
        if (_uiState.value.games.isEmpty()) {
            loadPage(reset = true, forceRefresh = true)
        } else {
            loadMore()
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isLoadingMore || state.isRefreshing || state.isEndReached) return
        loadPage(reset = false)
    }

    private fun loadPage(reset: Boolean, forceRefresh: Boolean = false) {
        loadJob?.cancel()
        if (reset) nextOffset = 0

        val query = _uiState.value.query
        val offset = nextOffset

        _uiState.update {
            it.copy(
                isInitialLoading = reset && it.games.isEmpty(),
                isRefreshing = reset && it.games.isNotEmpty(),
                isLoadingMore = !reset,
                errorMessage = null,
            )
        }

        loadJob = viewModelScope.launch {
            getGamesPage(query, offset, PAGE_SIZE, forceRefresh).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit

                    is NetworkResult.Success -> {
                        val page = result.data
                        nextOffset = offset + page.size
                        _uiState.update { state ->
                            state.copy(
                                games = if (reset) page else state.games + page,
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                isFromCache = result.fromCache,
                                isEndReached = page.size < PAGE_SIZE,
                                errorMessage = null,
                            )
                        }
                    }

                    is NetworkResult.Error -> _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            errorMessage = ErrorMapper.toUserMessage(result.throwable),
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 20

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { GameListViewModel(ServiceLocator.getGamesPageUseCase) }
        }
    }
}
