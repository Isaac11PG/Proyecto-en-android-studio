package com.example.mentalhealthapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var rootView: android.view.View
    private lateinit var switchBreathing: Switch
    private lateinit var switchJournal: Switch
    private lateinit var switchMotivation: Switch
    private val scheduler by lazy { NotificationScheduler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración"

        rootView = findViewById(android.R.id.content)
        switchBreathing = findViewById(R.id.switchBreathing)
        switchJournal = findViewById(R.id.switchJournal)
        switchMotivation = findViewById(R.id.switchMotivation)

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        switchBreathing.isChecked = prefs.getBoolean("notif_breathing_enabled", true)
        switchJournal.isChecked = prefs.getBoolean("notif_journal_enabled", true)
        switchMotivation.isChecked = prefs.getBoolean("notif_motivation_enabled", true)
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)

        switchBreathing.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif_breathing_enabled", isChecked).apply()
            if (isChecked) {
                scheduler.scheduleBreathingReminders()
                Snackbar.make(rootView, "✓ Recordatorios de respiración activados", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(rootView, "Recordatorios de respiración desactivados", Snackbar.LENGTH_SHORT).show()
            }
        }

        switchJournal.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif_journal_enabled", isChecked).apply()
            if (isChecked) {
                scheduler.scheduleJournalReminder()
                Snackbar.make(rootView, "✓ Recordatorios de diario activados", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(rootView, "Recordatorios de diario desactivados", Snackbar.LENGTH_SHORT).show()
            }
        }

        switchMotivation.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notif_motivation_enabled", isChecked).apply()
            if (isChecked) {
                scheduler.scheduleMotivationalMessages()
                Snackbar.make(rootView, "✓ Mensajes motivacionales activados", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(rootView, "Mensajes motivacionales desactivados", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}