package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class QuranReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "quran_reminder_channel"
        const val CHANNEL_NAME = "Pengingat Tadarus Al-Qur'an"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("quran_doa_prefs", Context.MODE_PRIVATE)
        
        // Match Duolingo style personalized messages or motivational reminders
        val isEnabled = prefs.getBoolean("quran_reminder_enabled", false)
        if (!isEnabled) return

        // Automatically schedule next day's reminder
        QuranReminderScheduler.scheduleReminder(context)

        val lastSurahName = prefs.getString("last_read_surah_name", "") ?: ""
        val lastVerseNo = prefs.getInt("last_read_verse_no", 0)

        // Streak check
        val rawDates = prefs.getString("quran_read_dates", "") ?: ""
        val streak = calculateStreakCount(rawDates)

        val title = if (streak > 0) {
            "Pertahankan konsistensi mendengarkan/membaca Al-Qur'an Anda! 🔥"
        } else {
            "Waktunya Tadarus Al-Qur'an Hari Ini"
        }

        val text = if (lastSurahName.isNotEmpty() && lastVerseNo > 0) {
            "Terakhir dibaca: QS. $lastSurahName ayat $lastVerseNo. Lanjutkan tilawah Anda sekarang!"
        } else {
            "Mari mulai membaca Al-Qur'an hari ini untuk menenangkan hati dan pikiran."
        }

        showNotification(context, title, text)
    }

    private fun showNotification(context: Context, title: String, text: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat harian konsistensi membaca Al-Qur'an"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target_screen", "quran")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9998,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(777, notification)
    }

    // Helper to calculate streak locally matching viewmodel
    private fun calculateStreakCount(rawDates: String): Int {
        if (rawDates.isEmpty()) return 0
        try {
            val dateList = rawDates.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            if (dateList.isEmpty()) return 0

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val todayStr = sdf.format(java.util.Date())
            
            // Check yesterday
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)

            // If neither today nor yesterday is in the set, streak is 0
            if (!dateList.contains(todayStr) && !dateList.contains(yesterdayStr)) {
                return 0
            }

            var streak = 0
            val checkCal = java.util.Calendar.getInstance()
            // If they read today, start checking from today
            if (dateList.contains(todayStr)) {
                // Keep checking backward
                while (true) {
                    val dateToCheck = sdf.format(checkCal.time)
                    if (dateList.contains(dateToCheck)) {
                        streak++
                        checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            } else {
                // Otherwise start checking from yesterday
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                while (true) {
                    val dateToCheck = sdf.format(checkCal.time)
                    if (dateList.contains(dateToCheck)) {
                        streak++
                        checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            }
            return streak
        } catch (e: Exception) {
            return 0
        }
    }
}
