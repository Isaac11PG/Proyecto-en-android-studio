package com.example.mentalhealthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_BREATHING_REMINDER = "com.example.mentalhealthapp.BREATHING_REMINDER"
        const val ACTION_JOURNAL_REMINDER = "com.example.mentalhealthapp.JOURNAL_REMINDER"
        const val ACTION_MOTIVATION = "com.example.mentalhealthapp.MOTIVATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received: ${intent.action}")

        val notificationHelper = NotificationHelper(context)

        when (intent.action) {
            ACTION_BREATHING_REMINDER -> {
                notificationHelper.sendBreathingReminder()
            }
            ACTION_JOURNAL_REMINDER -> {
                notificationHelper.sendJournalReminder()
            }
            ACTION_MOTIVATION -> {
                val messages = listOf(
                    "Cada día es una nueva oportunidad para cuidarte 💚",
                    "Tu bienestar mental es importante 🌟",
                    "Recuerda: está bien tomarte un descanso ☕",
                    "Eres más fuerte de lo que piensas 💪",
                    "Hoy es un buen día para ser amable contigo mismo 🌸",
                    "Tu progreso, por pequeño que sea, es valioso ✨",
                    "Respira. Estás haciendo lo mejor que puedes 🌬️",
                    "Tus emociones son válidas. Permítete sentirlas 💙"
                )
                notificationHelper.sendMotivationalMessage(messages.random())
            }
        }
    }
}