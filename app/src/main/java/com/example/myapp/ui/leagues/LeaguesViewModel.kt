package com.example.myapp.ui.leagues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.core.ErrorMapper
import com.example.myapp.core.NetworkResult
import com.example.myapp.di.ServiceLocator
import com.example.myapp.domain.model.League
import com.example.myapp.domain.usecase.GetLeaguesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaguesUiState(
    val leagues: List<League> = emptyList(),
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val errorMessage: String? = null,
)

/** ViewModel списка лиг (/leagues). */
class LeaguesViewModel(
    private val getLeagues: GetLeaguesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaguesUiState())
    val uiState: StateFlow<LeaguesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getLeagues(forceRefresh).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }

                    is NetworkResult.Success -> _uiState.update {
                        it.copy(
                            leagues = result.data,
                            isLoading = false,
                            isFromCache = result.fromCache,
                            errorMessage = null,
                        )
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

    fun refresh() = load(forceRefresh = true)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { LeaguesViewModel(ServiceLocator.getLeaguesUseCase) }
        }
    }
}
