package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

object QuranReminderScheduler {
    private const val TAG = "QuranReminderScheduler"

    fun scheduleReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val prefs = context.getSharedPreferences("quran_doa_prefs", Context.MODE_PRIVATE)
        
        val isEnabled = prefs.getBoolean("quran_reminder_enabled", false)
        if (!isEnabled) {
            cancelReminder(context)
            return
        }

        val hour = prefs.getInt("quran_reminder_hour", 18)
        val minute = prefs.getInt("quran_reminder_minute", 0)

        val now = Calendar.getInstance()
        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the reminder time has already passed today, schedule for tomorrow
        if (triggerTime.before(now)) {
            triggerTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, QuranReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999, // Unique request code for Quran reminder
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Quran reminder scheduled successfully at %02d:%02d using setAndAllowWhileIdle".format(hour, minute))
        } catch (e: Exception) {
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Quran reminder scheduled using fallback set at %02d:%02d".format(hour, minute))
            } catch (e2: Exception) {
                Log.e(TAG, "Fatal error scheduling Quran reminder alarm", e2)
            }
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, QuranReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Quran reminder cancelled successfully")
        }
    }
}
