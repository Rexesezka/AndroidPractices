package com.example.myapp.ui.profile

import com.example.myapp.domain.usecase.DownloadResumeUseCase
import com.example.myapp.domain.usecase.ObserveProfileUseCase
import com.example.myapp.domain.usecase.SaveAvatarUseCase
import com.example.myapp.domain.usecase.SaveProfileUseCase
import com.example.myapp.domain.usecase.ScheduleLessonReminderUseCase

/**
 * Зависимости фичи «Профиль».
 *
 * Модуль `:feature:profile` не знает про DI-контейнер приложения: зависимости
 * приходят снаружи (из `:app`) — поэтому направление зависимостей остаётся
 * `app -> feature -> core`.
 */
data class ProfileDependencies(
    val observeProfile: ObserveProfileUseCase,
    val saveProfile: SaveProfileUseCase,
    val saveAvatar: SaveAvatarUseCase,
    val downloadResume: DownloadResumeUseCase,
    val scheduleLessonReminder: ScheduleLessonReminderUseCase,
)
