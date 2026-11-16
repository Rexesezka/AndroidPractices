package com.example.myapp.data.local.profile

import android.content.Context
import com.example.myapp.domain.model.Profile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** Сериализуемое представление профиля для хранения в файле. */
@Serializable
internal data class ProfileDto(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = "",
)

internal fun ProfileDto.toDomain(): Profile = Profile(
    fullName = fullName,
    position = position,
    avatarUri = avatarUri,
    resumeUrl = resumeUrl,
)

internal fun Profile.toDto(): ProfileDto = ProfileDto(
    fullName = fullName,
    position = position,
    avatarUri = avatarUri,
    resumeUrl = resumeUrl,
)

/**
 * Хранение профиля в JSON-файле во внутреннем хранилище приложения
 * (`filesDir/profile.json`).
 *
 * Почему файл: структура профиля небольшая и фиксированная, а сам профиль —
 * это одна запись. JSON-файл читается/пишется целиком, легко расширяется
 * новыми полями и не требует миграций БД. Запись атомарная (через temp-файл).
 */
class ProfileFileDataSource(
    context: Context,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val file = File(context.filesDir, FILE_NAME)
    private val mutex = Mutex()

    /** null — профиль ещё не прочитан с диска. */
    private val state = MutableStateFlow<Profile?>(null)

    /**
     * Наблюдение за профилем. Первое чтение выполняется в IO-диспатчере,
     * поэтому вызывающий код всегда получает данные с диска, а не пустую заглушку.
     */
    val profile: Flow<Profile> = flow {
        if (state.value == null) {
            withContext(ioDispatcher) {
                mutex.withLock {
                    if (state.value == null) state.value = readFromFile()
                }
            }
        }
        emitAll(state.filterNotNull())
    }

    suspend fun save(profile: Profile) = withContext(ioDispatcher) {
        mutex.withLock {
            writeToFile(profile)
            state.value = profile
        }
    }

    private fun readFromFile(): Profile {
        if (!file.exists()) return Profile()
        return runCatching { json.decodeFromString<ProfileDto>(file.readText()).toDomain() }
            .getOrDefault(Profile())
    }

    private fun writeToFile(profile: Profile) {
        val payload = json.encodeToString(ProfileDto.serializer(), profile.toDto())
        val tmp = File(file.parentFile, "$FILE_NAME.tmp")
        tmp.writeText(payload)
        if (!tmp.renameTo(file)) {
            file.writeText(payload)
            tmp.delete()
        }
    }

    companion object {
        private const val FILE_NAME = "profile.json"
    }
}
