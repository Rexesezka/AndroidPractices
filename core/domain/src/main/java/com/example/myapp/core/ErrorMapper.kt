package com.example.myapp.core

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Преобразует исключения в понятные пользователю сообщения.
 * Живёт в доменном модуле, поэтому не зависит от сетевых библиотек.
 */
object ErrorMapper {

    fun toUserMessage(throwable: Throwable): String = when (throwable) {
        is UnknownHostException -> "Нет подключения к интернету"
        is SocketTimeoutException -> "Превышено время ожидания. Проверьте соединение"
        is IOException -> "Ошибка сети. Проверьте подключение"
        else -> throwable.message ?: "Неизвестная ошибка"
    }
}
