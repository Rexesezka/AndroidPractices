package com.example.myapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.core.ErrorMapper
import com.example.myapp.core.NetworkResult
import com.example.myapp.di.ServiceLocator
import com.example.myapp.domain.model.GameDetails
import com.example.myapp.domain.usecase.GetGameDetailsUseCase
import com.example.myapp.domain.usecase.ObserveIsFavoriteUseCase
import com.example.myapp.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailsUiState(
    val details: GameDetails? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * ViewModel экрана деталей матча. Id матча приходит из аргументов навигации
 * (path-параметр /games/{id}). Здесь же — добавление в избранное (Room).
 */
class GameDetailsViewModel(
    private val gameId: Int,
    private val getGameDetails: GetGameDetailsUseCase,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            observeIsFavorite(gameId).collect { _isFavorite.value = it }
        }
    }

    fun toggleFavorite() {
        val game = _uiState.value.details?.game ?: return
        viewModelScope.launch { toggleFavoriteUseCase(game) }
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getGameDetails(gameId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }

                    is NetworkResult.Success -> _uiState.update {
                        it.copy(details = result.data, isLoading = false, errorMessage = null)
                    }

                    is NetworkResult.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.toUserMessage(result.throwable),
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(gameId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameDetailsViewModel(
                    gameId = gameId,
                    getGameDetails = ServiceLocator.getGameDetailsUseCase,
                    observeIsFavorite = ServiceLocator.observeIsFavoriteUseCase,
                    toggleFavoriteUseCase = ServiceLocator.toggleFavoriteUseCase,
                )
            }
        }
    }
}
