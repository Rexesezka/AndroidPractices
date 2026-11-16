package com.example.myapp.domain.reminder

import com.example.myapp.domain.model.LessonReminder

/**
 * Планировщик локальных напоминаний (AlarmManager + BroadcastReceiver).
 * Доменный слой не знает, как именно планируется будильник.
 */
interface LessonReminderScheduler {

    /**
     * Запланировать напоминание на ближайшее наступление времени [time] (HH:mm):
     * сегодня, а если это время уже прошло — на завтра.
     */
    fun schedule(name: String, time: String): LessonReminder

    /** Отменить ранее запланированное напоминание. */
    fun cancel()
}
