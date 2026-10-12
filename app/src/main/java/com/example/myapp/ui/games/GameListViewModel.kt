package com.example.myapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.FootballRepository
import com.example.myapp.data.MockFootballRepository
import com.example.myapp.data.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameListUiState(
    val games: List<Game> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isEndReached: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel списка матчей. Реализует постраничную загрузку (offset/limit).
 */
class GameListViewModel(
    private val repository: FootballRepository = MockFootballRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameListUiState())
    val uiState: StateFlow<GameListUiState> = _uiState.asStateFlow()

    private var nextOffset = 0

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isLoadingMore || state.isEndReached) return

        _uiState.update {
            it.copy(
                isInitialLoading = it.games.isEmpty(),
                isLoadingMore = it.games.isNotEmpty(),
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching { repository.getGames(offset = nextOffset, limit = PAGE_SIZE) }
                .onSuccess { page ->
                    nextOffset += page.size
                    _uiState.update {
                        it.copy(
                            games = it.games + page,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            isEndReached = page.size < PAGE_SIZE,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isLoadingMore = false,
                            errorMessage = error.message ?: "Не удалось загрузить матчи",
                        )
                    }
                }
        }
    }

    fun retry() {
        if (_uiState.value.games.isEmpty()) {
            nextOffset = 0
        }
        loadNextPage()
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
