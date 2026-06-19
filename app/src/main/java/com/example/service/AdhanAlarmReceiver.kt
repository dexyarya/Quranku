package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AdhanAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "adhan_alarm_channel"
        const val CHANNEL_NAME = "Jadwal Shalat Al-Adhan"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "Shalat"
        val isEnabled = isPrayerAlarmEnabled(context, prayerName)
        
        if (!isEnabled) {
            return
        }

        showAdhanNotification(context, prayerName)
        playAdhanBeep(context)
    }

    private fun isPrayerAlarmEnabled(context: Context, prayerName: String): Boolean {
        val prefs = context.getSharedPreferences("khutbah_prefs", Context.MODE_PRIVATE)
        val overallEnabled = prefs.getBoolean("is_adhan_enabled", true)
        if (!overallEnabled) return false

        val key = when (prayerName.lowercase()) {
            "subuh", "fajr" -> "alarm_fajr"
            "terbit", "sunrise", "syuruq" -> "alarm_sunrise"
            "dzuhur", "dhuhr" -> "alarm_dhuhr"
            "ashar", "asr" -> "alarm_asr"
            "maghrib" -> "alarm_maghrib"
            "isya", "isha" -> "alarm_isha"
            else -> "is_adhan_enabled"
        }
        return prefs.getBoolean(key, true)
    }

    private fun showAdhanNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi alarm adzan jadwal shalat 5 waktu dan terbit matahari"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target_screen", "shalat")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isSunrise = prayerName.lowercase() in listOf("terbit", "sunrise", "syuruq")
        val title = if (isSunrise) "Waktu Terbit Matahari (Syuruq)" else "Waktu Shalat Bergetar!"
        val text = if (isSunrise) "Saatnya waktu Syuruq/Terbit. Batas waktu Shalat Subuh telah habis." else "Al-Adhan: Saatnya menunaikan shalat $prayerName."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }

    private fun playAdhanBeep(context: Context) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()

            // Stop play after 10 seconds to not disturb permanently
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    if (ringtone?.isPlaying == true) {
                        ringtone.stop()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 10000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
