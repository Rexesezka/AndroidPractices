package com.example.myapp.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapp.core.NotificationExtras
import com.example.myapp.core.data.R

/**
 * Создание канала уведомлений и показ напоминания о начале любимой пары.
 */
class NotificationHelper(private val context: Context) {

    private val manager: NotificationManagerCompat
        get() = NotificationManagerCompat.from(context)

    /** Канал нужно создать до первого уведомления (Android 8+). */
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val systemManager = context.getSystemService(NotificationManager::class.java) ?: return
        if (systemManager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминание о паре",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Напоминания о начале любимой пары"
            enableVibration(true)
        }
        systemManager.createNotificationChannel(channel)
    }

    /** Показывает уведомление «скоро пара» с именем владельца профиля. */
    fun showLessonReminder(name: String, time: String) {
        ensureChannel()

        // Модуль core:data не знает про app, поэтому получаем launch-intent пакета.
        val launchIntent = (
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
            ).apply {
            putExtra(NotificationExtras.EXTRA_OPEN_PROFILE, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Скоро любимая пара!")
            .setContentText("$name, не забудьте: пара начинается в $time")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$name, не забудьте: любимая пара начинается в $time. Удачи!"),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    companion object {
        const val CHANNEL_ID = "lesson_reminder"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE_OPEN = 2001
    }
}
