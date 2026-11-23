package com.example.myapp.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Принимает срабатывание будильника от AlarmManager и показывает
 * пуш-уведомление о начале любимой пары.
 */
class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) return

        val name = intent.getStringExtra(EXTRA_NAME).orEmpty().ifBlank { "Студент" }
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()

        NotificationHelper(context.applicationContext).showLessonReminder(name, time)
    }

    companion object {
        const val ACTION_LESSON_REMINDER = "com.example.myapp.action.LESSON_REMINDER"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_TIME = "extra_time"
    }
}
