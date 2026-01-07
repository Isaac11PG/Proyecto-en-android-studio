package com.example.mentalhealthapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleBreathingReminders() {
        // Recordatorio a las 10:00 AM
        scheduleNotification(
            NotificationReceiver.ACTION_BREATHING_REMINDER,
            10, 0,
            REQUEST_CODE_BREATHING_MORNING
        )

        // Recordatorio a las 3:00 PM
        scheduleNotification(
            NotificationReceiver.ACTION_BREATHING_REMINDER,
            15, 0,
            REQUEST_CODE_BREATHING_AFTERNOON
        )

        // Recordatorio a las 8:00 PM
        scheduleNotification(
            NotificationReceiver.ACTION_BREATHING_REMINDER,
            20, 0,
            REQUEST_CODE_BREATHING_EVENING
        )
    }

    fun scheduleJournalReminder() {
        // Recordatorio diario a las 9:00 PM
        scheduleNotification(
            NotificationReceiver.ACTION_JOURNAL_REMINDER,
            21, 0,
            REQUEST_CODE_JOURNAL
        )
    }

    fun scheduleMotivationalMessages() {
        // Mensaje motivacional a las 8:00 AM
        scheduleNotification(
            NotificationReceiver.ACTION_MOTIVATION,
            8, 0,
            REQUEST_CODE_MOTIVATION_MORNING
        )

        // Mensaje motivacional a las 12:00 PM
        scheduleNotification(
            NotificationReceiver.ACTION_MOTIVATION,
            12, 0,
            REQUEST_CODE_MOTIVATION_NOON
        )

        // Mensaje motivacional a las 6:00 PM
        scheduleNotification(
            NotificationReceiver.ACTION_MOTIVATION,
            18, 0,
            REQUEST_CODE_MOTIVATION_EVENING
        )
    }

    private fun scheduleNotification(action: String, hour: Int, minute: Int, requestCode: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // --- LOG DE PROGRAMACIÓN ---
        Log.d("NotificationScheduler",
            "Programando $action para ${calendar.time} (requestCode: $requestCode)")

        try {
            // Usar setInexactRepeating para ahorrar batería
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )

            // --- LOG DE ÉXITO ---
            Log.d("NotificationScheduler", "Alarma programada exitosamente para $action")

        } catch (e: SecurityException) {
            // --- LOG DE ERROR ---
            Log.e("NotificationScheduler", "Error: Sin permiso para alarmas exactas", e)
        }
    }

    fun cancelAllNotifications() {
        val requestCodes = listOf(
            REQUEST_CODE_BREATHING_MORNING,
            REQUEST_CODE_BREATHING_AFTERNOON,
            REQUEST_CODE_BREATHING_EVENING,
            REQUEST_CODE_JOURNAL,
            REQUEST_CODE_MOTIVATION_MORNING,
            REQUEST_CODE_MOTIVATION_NOON,
            REQUEST_CODE_MOTIVATION_EVENING
        )

        requestCodes.forEach { requestCode ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }
        Log.d("NotificationScheduler", "Todas las notificaciones han sido canceladas")
    }

    companion object {
        private const val REQUEST_CODE_BREATHING_MORNING = 100
        private const val REQUEST_CODE_BREATHING_AFTERNOON = 101
        private const val REQUEST_CODE_BREATHING_EVENING = 102
        private const val REQUEST_CODE_JOURNAL = 200
        private const val REQUEST_CODE_MOTIVATION_MORNING = 300
        private const val REQUEST_CODE_MOTIVATION_NOON = 301
        private const val REQUEST_CODE_MOTIVATION_EVENING = 302
    }
}