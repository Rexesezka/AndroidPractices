package com.example.myapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.core.ErrorMapper
import com.example.myapp.domain.model.DownloadedDocument
import com.example.myapp.domain.model.Profile
import com.example.myapp.domain.usecase.DownloadResumeUseCase
import com.example.myapp.domain.usecase.ObserveProfileUseCase
import com.example.myapp.domain.usecase.ScheduleLessonReminderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: Profile = Profile(),
    val isLoading: Boolean = true,
    val isDownloadingResume: Boolean = false,
    val errorMessage: String? = null,
    /** Скачанный документ, который нужно открыть просмотрщиком (одноразовое событие). */
    val documentToOpen: DownloadedDocument? = null,
)

/**
 * ViewModel экрана профиля: наблюдает за профилем из локального хранилища
 * и скачивает резюме по ссылке.
 */
class ProfileViewModel(
    observeProfile: ObserveProfileUseCase,
    private val downloadResume: DownloadResumeUseCase,
    private val scheduleLessonReminder: ScheduleLessonReminderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Напоминание переустанавливается один раз за жизнь ViewModel. */
    private var reminderRestored = false

    init {
        viewModelScope.launch {
            observeProfile().collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }

                // Будильник мог быть потерян (перезагрузка, обновление приложения) —
                // при открытии профиля ставим его заново на то же время.
                if (!reminderRestored && Profile.isValidTime(profile.lessonTime)) {
                    reminderRestored = true
                    runCatching {
                        scheduleLessonReminder(
                            name = profile.fullName.ifBlank { "Студент" },
                            time = profile.lessonTime,
                        )
                    }
                }
            }
        }
    }

    fun openResume() {
        val url = _uiState.value.profile.resumeUrl
        if (url.isBlank() || _uiState.value.isDownloadingResume) return

        _uiState.update { it.copy(isDownloadingResume = true, errorMessage = null) }
        viewModelScope.launch {
            downloadResume(url)
                .onSuccess { document ->
                    _uiState.update { it.copy(isDownloadingResume = false, documentToOpen = document) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isDownloadingResume = false,
                            errorMessage = ErrorMapper.toUserMessage(throwable),
                        )
                    }
                }
        }
    }

    /** Документ передан просмотрщику — событие больше не нужно. */
    fun onDocumentOpened() {
        _uiState.update { it.copy(documentToOpen = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun factory(dependencies: ProfileDependencies): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    observeProfile = dependencies.observeProfile,
                    downloadResume = dependencies.downloadResume,
                    scheduleLessonReminder = dependencies.scheduleLessonReminder,
                )
            }
        }
    }
}
