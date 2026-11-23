package com.example.myapp.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Простой потокобезопасный кэш в памяти с временем жизни записи (TTL).
 *
 * Значения хранятся в [MutableStateFlow], поэтому кэш можно наблюдать как Flow
 * (метод [observe]) — это используется для кэширования сетевых запросов.
 */
class InMemoryCache<T>(private val ttlMillis: Long) {

    private data class Entry<T>(val value: T, val timestamp: Long)

    private val entries = MutableStateFlow<Map<String, Entry<T>>>(emptyMap())

    /** Возвращает актуальное (не протухшее) значение или null. */
    fun get(key: String): T? {
        val entry = entries.value[key] ?: return null
        return if (System.currentTimeMillis() - entry.timestamp <= ttlMillis) entry.value else null
    }

    fun put(key: String, value: T) {
        entries.update { it + (key to Entry(value, System.currentTimeMillis())) }
    }

    /** Наблюдение за значением по ключу (для реактивного использования кэша). */
    fun observe(key: String): Flow<T?> = entries.map { it[key]?.value }

    fun clear() {
        entries.value = emptyMap()
    }
}
