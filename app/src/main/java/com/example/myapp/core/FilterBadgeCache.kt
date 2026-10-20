package com.example.myapp.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Общий для экрана списка и экрана настроек кэш состояния бейджа.
 *
 * Хранит только признак «есть активные настройки фильтра» в памяти:
 * долгосрочное хранение не требуется (при перезапуске значение
 * восстанавливается из сохранённых настроек).
 *
 * Создаётся через DI ([com.example.myapp.di.ServiceLocator]), поэтому
 * оба экрана работают с одним и тем же экземпляром.
 */
class FilterBadgeCache {

    private val _hasActiveFilters = MutableStateFlow(false)
    val hasActiveFilters: StateFlow<Boolean> = _hasActiveFilters.asStateFlow()

    fun update(hasActiveFilters: Boolean) {
        _hasActiveFilters.value = hasActiveFilters
    }
}
