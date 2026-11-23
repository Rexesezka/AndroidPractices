package com.example.myapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapp.domain.model.Profile
import com.example.myapp.domain.usecase.ObserveProfileUseCase
import com.example.myapp.domain.usecase.SaveAvatarUseCase
import com.example.myapp.domain.usecase.SaveProfileUseCase
import com.example.myapp.domain.usecase.ScheduleLessonReminderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ProfileEditUiState(
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String? = null,
    /** Время любимой пары в формате HH:mm. */
    val lessonTime: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAvatarProcessing: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    /** Текст о запланированном напоминании, показывается после сохранения. */
    val scheduledMessage: String? = null,
) {
    /** Время заполнено и соответствует формату HH:mm. */
    val isLessonTimeValid: Boolean
        get() = Profile.isValidTime(lessonTime)

    /** Текст ошибки под полем времени (показывается, пока формат неверный). */
    val lessonTimeError: String?
        get() = when {
            lessonTime.isBlank() -> "Укажите время пары в формате HH:mm"
            !isLessonTimeValid -> "Неверный формат времени. Нужно HH:mm, например 09:30"
            else -> null
        }

    /** Сохранение доступно только при корректном времени. */
    val canSave: Boolean
        get() = !isSaving && isLessonTimeValid
}

/**
 * ViewModel экрана редактирования профиля: держит черновик полей,
 * копирует выбранную аватарку в память телефона, сохраняет профиль
 * и планирует напоминание о начале любимой пары.
 */
class ProfileEditViewModel(
    observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val saveAvatar: SaveAvatarUseCase,
    private val scheduleLessonReminder: ScheduleLessonReminderUseCase,
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
                    lessonTime = profile.lessonTime,
                    isLoading = false,
                )
            }
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value) }

    fun updatePosition(value: String) = _uiState.update { it.copy(position = value) }

    fun updateResumeUrl(value: String) = _uiState.update { it.copy(resumeUrl = value) }

    /**
     * Ввод времени с клавиатуры: оставляем только цифры и автоматически
     * подставляем двоеточие — 0930 превращается в 09:30.
     */
    fun updateLessonTime(raw: String) {
        val digits = raw.filter(Char::isDigit).take(4)
        val formatted = if (digits.length <= 2) {
            digits
        } else {
            "${digits.take(2)}:${digits.drop(2)}"
        }
        _uiState.update { it.copy(lessonTime = formatted) }
    }

    /** Время, выбранное в виджете часов (TimePicker). */
    fun onTimePicked(hour: Int, minute: Int) {
        _uiState.update { it.copy(lessonTime = String.format(Locale.US, "%02d:%02d", hour, minute)) }
    }

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

        if (!state.isLessonTimeValid) {
            _uiState.update { it.copy(errorMessage = state.lessonTimeError) }
            return
        }

        val url = state.resumeUrl.trim()
        if (url.isNotEmpty() && !url.startsWith("https://") && !url.startsWith("http://")) {
            _uiState.update { it.copy(errorMessage = "Ссылка должна начинаться с https:// или http://") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                val profile = Profile(
                    fullName = state.fullName.trim(),
                    position = state.position.trim(),
                    avatarUri = state.avatarUri,
                    resumeUrl = url,
                    lessonTime = state.lessonTime,
                )
                saveProfile(profile)

                // Напоминание ставим на ближайшее наступление указанного времени.
                val name = profile.fullName.ifBlank { "Студент" }
                val reminder = scheduleLessonReminder(name = name, time = profile.lessonTime)
                formatTrigger(reminder.triggerAtMillis, reminder.time)
            }.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        errorMessage = null,
                        scheduledMessage = message,
                    )
                }
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

    private fun formatTrigger(triggerAtMillis: Long, time: String): String {
        val trigger = Calendar.getInstance().apply { this.timeInMillis = triggerAtMillis }
        val now = Calendar.getInstance()
        val dayLabel = if (trigger.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            "сегодня"
        } else {
            val date = SimpleDateFormat("d MMMM", Locale.forLanguageTag("ru")).format(Date(triggerAtMillis))
            "завтра, $date"
        }
        return "Напоминание на $time ($dayLabel)"
    }

    companion object {
        fun factory(dependencies: ProfileDependencies): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileEditViewModel(
                    observeProfile = dependencies.observeProfile,
                    saveProfile = dependencies.saveProfile,
                    saveAvatar = dependencies.saveAvatar,
                    scheduleLessonReminder = dependencies.scheduleLessonReminder,
                )
            }
        }
    }
}
