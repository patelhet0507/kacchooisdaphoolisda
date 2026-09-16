package com.example.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object DailyNotificationManager {
    private const val CHANNEL_ID = "kaachu_daily_reminder_channel"
    private const val NOTIFICATION_ID = 2026

    fun scheduleDailyReminder(context: Context) {
        try {
            createNotificationChannel(context)
            
            val prefs = context.getSharedPreferences("daily_notification_prefs", Context.MODE_PRIVATE)
            val lastShown = prefs.getLong("last_shown_date", 0L)
            val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            
            if (today > lastShown) {
                prefs.edit().putLong("last_shown_date", today).apply()
                showDailyNotification(context)
            }
        } catch (e: Exception) {
            android.util.Log.e("DailyNotificationManager", "Error scheduling daily reminder", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Play Reminders"
            val descriptionText = "Daily reminders to play Kaachu Phool and challenge friends"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyNotification(context: Context) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time to Play Kaachu Phool! 🎮")
                .setContentText("Your friends are waiting! Jump in and start a match today.")
                .setStyle(NotificationCompat.BigTextStyle().bigText("Your friends are waiting! Jump in and start a match today to keep your daily streak alive and climb the leaderboard."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            android.util.Log.e("DailyNotificationManager", "Error showing daily notification", e)
        }
    }
}
