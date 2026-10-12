package com.example.myapp.core

/**
 * Обёртка результата сетевой операции для слоя представления.
 */
sealed interface NetworkResult<out T> {

    /** Данные загружаются. */
    data object Loading : NetworkResult<Nothing>

    /** Успешный ответ. [fromCache] == true, если данные отданы из кэша. */
    data class Success<T>(val data: T, val fromCache: Boolean = false) : NetworkResult<T>

    /** Ошибка выполнения запроса. */
    data class Error(val throwable: Throwable) : NetworkResult<Nothing>
}
