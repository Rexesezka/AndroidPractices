package com.example.myapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.di.ServiceLocator
import com.example.myapp.domain.model.Profile
import com.example.myapp.domain.usecase.ObserveProfileUseCase
import com.example.myapp.domain.usecase.SaveAvatarUseCase
import com.example.myapp.domain.usecase.SaveProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAvatarProcessing: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
)

/**
 * ViewModel экрана редактирования профиля: держит черновик полей,
 * копирует выбранную аватарку в память телефона и сохраняет профиль.
 */
class ProfileEditViewModel(
    observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val saveAvatar: SaveAvatarUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = observeProfile().first()
            _uiState.update {
                it.copy(
                    fullName = profile.fullName,
                    position = profile.position,
                    resumeUrl = profile.resumeUrl,
                    avatarUri = profile.avatarUri,
                    isLoading = false,
                )
            }
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value) }

    fun updatePosition(value: String) = _uiState.update { it.copy(position = value) }

    fun updateResumeUrl(value: String) = _uiState.update { it.copy(resumeUrl = value) }

    /** Пользователь выбрал фото в галерее или сделал снимок камерой. */
    fun onAvatarSelected(sourceUri: String) {
        _uiState.update { it.copy(isAvatarProcessing = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { saveAvatar(sourceUri) }
                .onSuccess { storedUri ->
                    _uiState.update { it.copy(avatarUri = storedUri, isAvatarProcessing = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isAvatarProcessing = false,
                            errorMessage = throwable.message ?: "Не удалось сохранить аватарку",
                        )
                    }
                }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val url = state.resumeUrl.trim()
        if (url.isNotEmpty() && !url.startsWith("https://") && !url.startsWith("http://")) {
            _uiState.update { it.copy(errorMessage = "Ссылка должна начинаться с https:// или http://") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                saveProfile(
                    Profile(
                        fullName = state.fullName.trim(),
                        position = state.position.trim(),
                        avatarUri = state.avatarUri,
                        resumeUrl = url,
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Не удалось сохранить профиль",
                    )
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileEditViewModel(
                    observeProfile = ServiceLocator.observeProfileUseCase,
                    saveProfile = ServiceLocator.saveProfileUseCase,
                    saveAvatar = ServiceLocator.saveAvatarUseCase,
                )
            }
        }
    }
}
