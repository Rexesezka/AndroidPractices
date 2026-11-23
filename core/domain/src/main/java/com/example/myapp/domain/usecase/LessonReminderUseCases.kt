package com.example.myapp.domain.usecase

import com.example.myapp.domain.model.LessonReminder
import com.example.myapp.domain.reminder.LessonReminderScheduler

/** Планирование напоминания о начале любимой пары. */
class ScheduleLessonReminderUseCase(
    private val scheduler: LessonReminderScheduler,
) {
    operator fun invoke(name: String, time: String): LessonReminder = scheduler.schedule(name, time)
}

/** Отмена напоминания (например, при очистке времени в профиле). */
class CancelLessonReminderUseCase(
    private val scheduler: LessonReminderScheduler,
) {
    operator fun invoke() = scheduler.cancel()
}
