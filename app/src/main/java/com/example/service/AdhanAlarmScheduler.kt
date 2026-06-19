package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

object AdhanAlarmScheduler {
    private const val TAG = "AdhanAlarmScheduler"

    fun scheduleAlarms(context: Context, timings: Map<String, String>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val prayers = mapOf(
            "Fajr" to "Subuh",
            "Sunrise" to "Terbit",
            "Dhuhr" to "Dzuhur",
            "Asr" to "Ashar",
            "Maghrib" to "Maghrib",
            "Isha" to "Isya"
        )

        for ((key, name) in prayers) {
            val timeStr = timings[key] ?: continue
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toIntOrNull() ?: continue
                val minute = parts[1].split(" ")[0].trim().toIntOrNull() ?: continue
                
                // Construct trigger time
                val now = Calendar.getInstance()
                val triggerTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If the prayer time has already passed today, schedule for tomorrow
                if (triggerTime.before(now)) {
                    triggerTime.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", name)
                }

                val requestCode = name.hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled alarm for $name at ${triggerTime.time}")
                } catch (e: Exception) {
                    try {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime.timeInMillis,
                            pendingIntent
                        )
                    } catch (e2: Exception) {
                        Log.e(TAG, "Fatal error scheduling exact alarm for $name", e2)
                    }
                }
            }
        }
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val prayers = listOf("Subuh", "Terbit", "Dzuhur", "Ashar", "Maghrib", "Isya")
        
        for (name in prayers) {
            val intent = Intent(context, AdhanAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                name.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for $name")
            }
        }
    }
}
