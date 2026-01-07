package com.example.mentalhealthapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "reminders_channel"
        const val CHANNEL_MOTIVATION = "motivation_channel"
        const val CHANNEL_ACHIEVEMENTS = "achievements_channel"

        const val NOTIFICATION_BREATHING = 1
        const val NOTIFICATION_JOURNAL = 2
        const val NOTIFICATION_MOTIVATION = 3
        const val NOTIFICATION_ACHIEVEMENT = 4
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal de Recordatorios
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de ejercicios de respiración y diario"
                enableVibration(true)
            }

            // Canal de Motivación
            val motivationChannel = NotificationChannel(
                CHANNEL_MOTIVATION,
                "Mensajes Motivacionales",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mensajes diarios de motivación y ánimo"
                enableVibration(false)
            }

            // Canal de Logros
            val achievementsChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENTS,
                "Logros",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de logros y metas alcanzadas"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(motivationChannel)
            notificationManager.createNotificationChannel(achievementsChannel)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendBreathingReminder() {
        val intent = Intent(context, BreathingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🌬️ Hora de respirar")
            .setContentText("Toma 5 minutos para hacer ejercicios de respiración")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Toma un descanso y haz algunos ejercicios de respiración. Te ayudará a reducir el estrés y mejorar tu concentración."))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_BREATHING, notification)
    }

    @SuppressLint("MissingPermission")
    fun sendJournalReminder() {
        val intent = Intent(context, JournalActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📝 Tu diario te espera")
            .setContentText("¿Cómo te sientes hoy? Escribe tus pensamientos")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Escribir sobre tus emociones y experiencias puede ayudarte a procesarlas mejor. ¡Toma unos minutos para ti!"))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_JOURNAL, notification)
    }

    @SuppressLint("MissingPermission")
    fun sendMotivationalMessage(message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("✨ Mensaje del día")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_MOTIVATION, notification)
    }

    @SuppressLint("MissingPermission")
    fun sendAchievementNotification(achievement: String, description: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🏆 ¡Logro desbloqueado!")
            .setContentText(achievement)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$achievement\n\n$description"))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ACHIEVEMENT, notification)
    }
}