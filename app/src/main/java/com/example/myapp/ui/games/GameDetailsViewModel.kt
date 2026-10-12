package com.example.myapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.data.FootballRepository
import com.example.myapp.data.MockFootballRepository
import com.example.myapp.data.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailsUiState(
    val game: Game? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * ViewModel экрана деталей матча. Получает id матча из аргументов навигации.
 */
class GameDetailsViewModel(
    private val gameId: Int,
    private val repository: FootballRepository = MockFootballRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { repository.getGameById(gameId) }
                .onSuccess { game ->
                    _uiState.update {
                        it.copy(
                            game = game,
                            isLoading = false,
                            errorMessage = if (game == null) "Матч не найден" else null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Не удалось загрузить матч",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(gameId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameDetailsViewModel(gameId) }
        }
    }
}
