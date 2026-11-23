package com.example.myapp.domain.usecase

import com.example.myapp.domain.model.DownloadedDocument
import com.example.myapp.domain.model.Profile
import com.example.myapp.domain.repository.ProfileRepository
import com.example.myapp.domain.repository.ResumeRepository
import kotlinx.coroutines.flow.Flow

/** Наблюдение за сохранённым профилем. */
class ObserveProfileUseCase(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): Flow<Profile> = repository.observeProfile()
}

/** Сохранение профиля в память телефона. */
class SaveProfileUseCase(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(profile: Profile) = repository.saveProfile(profile)
}

/** Копирование выбранного изображения во внутреннее хранилище (аватарка). */
class SaveAvatarUseCase(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(sourceUri: String): String = repository.saveAvatar(sourceUri)
}

/** Скачивание резюме по ссылке. */
class DownloadResumeUseCase(
    private val repository: ResumeRepository,
) {
    suspend operator fun invoke(url: String): Result<DownloadedDocument> = repository.download(url)
}
