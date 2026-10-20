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
 * (path-параметр /games/{id}).
 */
class GameDetailsViewModel(
    private val gameId: Int,
    private val getGameDetails: GetGameDetailsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
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
            initializer { GameDetailsViewModel(gameId, ServiceLocator.getGameDetailsUseCase) }
        }
    }
}
