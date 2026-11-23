package com.example.myapp.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.myapp.domain.model.LessonReminder
import com.example.myapp.domain.reminder.LessonReminderScheduler
import java.util.Calendar

/**
 * Планировщик напоминаний на AlarmManager.
 *
 * Время срабатывания — ближайшее наступление указанного HH:mm: сегодня,
 * а если это время уже прошло — на завтра. На Android 12+ точные будильники
 * требуют разрешения SCHEDULE_EXACT_ALARM; если его нет, используем
 * неточный будильник (уведомление всё равно придёт, но с небольшим сдвигом).
 */
class AlarmLessonReminderScheduler(
    private val context: Context,
) : LessonReminderScheduler {

    override fun schedule(name: String, time: String): LessonReminder {
        val (hour, minute) = parseTime(time)
        val triggerAt = nextTriggerAt(hour, minute)

        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            action = LessonReminderReceiver.ACTION_LESSON_REMINDER
            putExtra(LessonReminderReceiver.EXTRA_NAME, name)
            putExtra(LessonReminderReceiver.EXTRA_TIME, time)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (alarmManager != null) {
            val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            if (canBeExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent,
                )
            } else {
                Log.w(TAG, "Нет разрешения на точные будильники — планируем неточный")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent,
                )
            }
        }

        return LessonReminder(name = name, time = time, triggerAtMillis = triggerAt)
    }

    override fun cancel() {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            action = LessonReminderReceiver.ACTION_LESSON_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        require(parts.size == 2) { "Неверный формат времени: $time" }
        val hour = parts[0].toIntOrNull() ?: error("Неверный час: $time")
        val minute = parts[1].toIntOrNull() ?: error("Неверные минуты: $time")
        require(hour in 0..23 && minute in 0..59) { "Неверное время: $time" }
        return hour to minute
    }

    /** Ближайшее наступление времени: сегодня или завтра, если уже прошло. */
    private fun nextTriggerAt(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    companion object {
        private const val TAG = "LessonReminder"
        private const val REQUEST_CODE = 3001
    }
}
