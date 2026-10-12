package com.example.myapp.core

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Преобразует исключения сетевого слоя в понятные пользователю сообщения.
 */
object ErrorMapper {

    fun toUserMessage(throwable: Throwable): String = when (throwable) {
        is UnknownHostException -> "Нет подключения к интернету"
        is SocketTimeoutException -> "Превышено время ожидания. Проверьте соединение"
        is IOException -> "Ошибка сети. Проверьте подключение"
        is HttpException -> "Ошибка сервера (код ${throwable.code()})"
        else -> throwable.message ?: "Неизвестная ошибка"
    }
}
