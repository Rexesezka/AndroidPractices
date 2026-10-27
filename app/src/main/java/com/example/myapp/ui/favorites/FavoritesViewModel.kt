package com.example.myapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.di.ServiceLocator
import com.example.myapp.domain.model.FavoriteGame
import com.example.myapp.domain.usecase.ObserveFavoritesUseCase
import com.example.myapp.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<FavoriteGame> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * ViewModel избранного. Данные берутся только из Room, поэтому
 * экран работает без подключения к интернету.
 */
class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = observeFavorites()
        .map { FavoritesUiState(favorites = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    fun remove(gameId: Int) {
        viewModelScope.launch { removeFavorite(gameId) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FavoritesViewModel(
                    observeFavorites = ServiceLocator.observeFavoritesUseCase,
                    removeFavorite = ServiceLocator.removeFavoriteUseCase,
                )
            }
        }
    }
}
