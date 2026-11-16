package com.example.myapp.domain.model

/**
 * Запланированное напоминание о начале любимой пары.
 *
 * [triggerAtMillis] — момент срабатывания (epoch millis), [time] — время в формате HH:mm.
 */
data class LessonReminder(
    val name: String,
    val time: String,
    val triggerAtMillis: Long,
)
