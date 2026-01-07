package com.example.mentalhealthapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class TestNotificationsActivity : AppCompatActivity() {

    private lateinit var rootView: android.view.View
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_notifications)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Prueba de Notificaciones"

        rootView = findViewById(android.R.id.content)
        statusText = findViewById(R.id.statusText)

        checkNotificationStatus()

        findViewById<Button>(R.id.btnTestBreathing).setOnClickListener {
            NotificationHelper(this).sendBreathingReminder()
            Snackbar.make(rootView, "Notificación de respiración enviada", Snackbar.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTestJournal).setOnClickListener {
            NotificationHelper(this).sendJournalReminder()
            Snackbar.make(rootView, "Notificación de diario enviada", Snackbar.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTestMotivation).setOnClickListener {
            NotificationHelper(this).sendMotivationalMessage("Esta es una prueba ✨")
            Snackbar.make(rootView, "Mensaje motivacional enviado", Snackbar.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTestAchievement).setOnClickListener {
            NotificationHelper(this).sendAchievementNotification(
                "Logro de Prueba",
                "¡Has probado las notificaciones correctamente!"
            )
            Snackbar.make(rootView, "Notificación de logro enviada", Snackbar.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnScheduleAlarms).setOnClickListener {
            val scheduler = NotificationScheduler(this)
            scheduler.scheduleBreathingReminders()
            scheduler.scheduleJournalReminder()
            scheduler.scheduleMotivationalMessages()

            Snackbar.make(rootView, "Alarmas programadas", Snackbar.LENGTH_LONG).show()
            checkNotificationStatus()
        }
    }

    private fun checkNotificationStatus() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val areEnabled = notificationManager.areNotificationsEnabled()

        val status = buildString {
            append("Estado de notificaciones:\n\n")
            append("✓ Permiso general: ${if (areEnabled) "Activado" else "DESACTIVADO"}\n\n")

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channels = notificationManager.notificationChannels
                append("Canales (${channels.size}):\n")
                channels.forEach { channel ->
                    append("• ${channel.name}: ${if (channel.importance != android.app.NotificationManager.IMPORTANCE_NONE) "Activo" else "Inactivo"}\n")
                }
            }
        }

        statusText.text = status
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}