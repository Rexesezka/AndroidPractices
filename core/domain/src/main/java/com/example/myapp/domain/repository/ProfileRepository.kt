package com.example.myapp.domain.repository

import com.example.myapp.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Хранилище профиля пользователя на устройстве.
 */
interface ProfileRepository {

    /** Наблюдение за профилем (обновляется после сохранения). */
    fun observeProfile(): Flow<Profile>

    /** Сохранить профиль в память телефона. */
    suspend fun saveProfile(profile: Profile)

    /**
     * Скопировать выбранное пользователем изображение во внутреннее хранилище
     * приложения и вернуть URI сохранённого файла.
     */
    suspend fun saveAvatar(sourceUri: String): String
}
