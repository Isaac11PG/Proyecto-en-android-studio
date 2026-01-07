package com.example.mentalhealthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)

            // Solo reconfigurar si el usuario ya configuró las notificaciones antes
            if (prefs.getBoolean("notifications_configured", false)) {
                val scheduler = NotificationScheduler(context)
                scheduler.scheduleBreathingReminders()
                scheduler.scheduleJournalReminder()
                scheduler.scheduleMotivationalMessages()
            }
        }
    }
}