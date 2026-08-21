package com.blockme.app.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.blockme.app.MainActivity
import com.blockme.app.R
import com.blockme.core.common.Constants
import com.blockme.core.common.toFormattedTime
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates and manages notification channels and persistent session notification.
 * Uses dedicated custom vector status bar icon [R.drawable.ic_notification].
 *
 * SPDX-License-Identifier: MIT
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val accentColor by lazy {
        ContextCompat.getColor(context, R.color.accent_primary)
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        val sessionChannel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_SESSION,
            "Focus Session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows your active focus session status"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val reminderChannel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_REMINDER,
            "Session Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders before scheduled sessions start"
        }

        notificationManager.createNotificationChannels(listOf(sessionChannel, reminderChannel))
    }

    /**
     * Build the persistent foreground service notification for an active session.
     */
    fun buildSessionNotification(
        remainingMs: Long,
        goalText: String
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeStr = remainingMs.toFormattedTime()
        val contentText = if (goalText.isNotBlank()) "$timeStr · $goalText" else timeStr

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_SESSION)
            .setContentTitle("Focus Session Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * Update the persistent session notification in-place (no re-post sound).
     */
    fun updateSessionNotification(remainingMs: Long, goalText: String) {
        val notification = buildSessionNotification(remainingMs, goalText)
        notificationManager.notify(Constants.NOTIFICATION_ID_SESSION, notification)
    }

    /**
     * Build and show a reminder notification before a scheduled session.
     */
    fun showReminderNotification(goalText: String, durationLabel: String) {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDER)
            .setContentTitle("Focus session starting soon")
            .setContentText("Your $durationLabel focus session starts in 5 minutes${if (goalText.isNotBlank()) " · $goalText" else ""}")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID_REMINDER, notification)
    }

    fun showCompletionNotification() {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDER)
            .setContentTitle("Focus Session Complete! 🎉")
            .setContentText("You completed your focus session without unlocking. Great job!")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID_SESSION + 1, notification)
    }

    fun cancelSessionNotification() {
        notificationManager.cancel(Constants.NOTIFICATION_ID_SESSION)
        notificationManager.cancel(Constants.NOTIFICATION_ID_SESSION + 2)
    }
}
