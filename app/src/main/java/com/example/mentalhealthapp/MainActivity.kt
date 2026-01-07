package com.example.mentalhealthapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rootView: android.view.View
    private lateinit var database: AppDatabase
    private var selectedMood: String? = null
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView = findViewById(android.R.id.content)
        database = AppDatabase.getDatabase(this)

        val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userName = sharedPreferences.getString("user_name", "Usuario")
        val textUserName = findViewById<TextView>(R.id.textUserName)
        textUserName.text = "Hola, $userName 👋"

        // ← BOTÓN DE PRUEBA (Manten presionando el nombre de usuario)
        textUserName.setOnLongClickListener {
            testNotifications()
            true
        }

        // Emojis de estado de ánimo
        findViewById<TextView>(R.id.emoji1).setOnClickListener { selectMood("Triste 😢") }
        findViewById<TextView>(R.id.emoji2).setOnClickListener { selectMood("Normal 😐") }
        findViewById<TextView>(R.id.emoji3).setOnClickListener { selectMood("Bien 🙂") }
        findViewById<TextView>(R.id.emoji4).setOnClickListener { selectMood("Feliz 😊") }

        // Tarjetas de actividades
        findViewById<CardView>(R.id.cardMeditation).setOnClickListener {
            startActivity(Intent(this, MeditationActivity::class.java))
        }

        findViewById<CardView>(R.id.cardBreathing).setOnClickListener {
            startActivity(Intent(this, BreathingActivity::class.java))
        }

        findViewById<CardView>(R.id.cardJournal).setOnClickListener {
            val intent = Intent(this, JournalActivity::class.java).apply {
                selectedMood?.let { putExtra("mood", it) }
            }
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardGames).setOnClickListener {
            startActivity(Intent(this, GamesMenuActivity::class.java))
        }

        // Configuración y Logout
        findViewById<Button>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.buttonLogout)?.setOnClickListener {
            showLogoutDialog()
        }

        // Configurar notificaciones si es la primera vez
        if (!sharedPreferences.getBoolean("notifications_configured", false)) {
            setupNotifications()
            sharedPreferences.edit().putBoolean("notifications_configured", true).apply()
        }

        showStats()
    }

    // ← MÉTODO DE PRUEBA DE NOTIFICACIONES
    private fun testNotifications() {
        val notificationHelper = NotificationHelper(this)

        Snackbar.make(rootView, "Probando notificaciones en secuencia...", Snackbar.LENGTH_LONG).show()

        // 1. Inmediata
        notificationHelper.sendBreathingReminder()

        // 2. A los 3 segundos
        rootView.postDelayed({
            notificationHelper.sendJournalReminder()
        }, 3000)

        // 3. A los 6 segundos
        rootView.postDelayed({
            notificationHelper.sendMotivationalMessage("¡Estás haciendo un gran trabajo! 💪")
        }, 6000)

        // 4. A los 9 segundos
        rootView.postDelayed({
            notificationHelper.sendAchievementNotification(
                "Probador Experto",
                "¡Has probado todas las notificaciones correctamente!"
            )
        }, 9000)
    }

    private fun selectMood(mood: String) {
        selectedMood = mood
        lifecycleScope.launch {
            database.userDao().updateMood(currentUserId, mood)
            Snackbar.make(rootView, "Estado de ánimo: $mood", Snackbar.LENGTH_SHORT)
                .setAction("Escribir") {
                    val intent = Intent(this@MainActivity, JournalActivity::class.java).apply {
                        putExtra("mood", mood)
                    }
                    startActivity(intent)
                }.show()
        }
    }

    private fun showStats() {
        lifecycleScope.launch {
            val stats = database.userStatsDao().getStats(currentUserId)

            stats?.let { userStats -> // Usamos userStats en lugar de "it" para evitar errores
                if (userStats.totalJournalEntries > 0 || userStats.totalBreathingSessions > 0 || userStats.totalBubblesPopped > 0) {
                    val message = buildString {
                        append("📊 Tus estadísticas:\n")
                        if (userStats.totalJournalEntries > 0) append("📝 ${userStats.totalJournalEntries} entradas\n")
                        if (userStats.totalBreathingSessions > 0) append("🌬️ ${userStats.totalBreathingSessions} sesiones\n")
                        if (userStats.totalBubblesPopped > 0) append("🫧 ${userStats.totalBubblesPopped} burbujas")
                    }

                    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                        .setAction("Ver más") {
                            showDetailedStats(userStats) // Corregido el error de referencia
                        }.show()
                }
            }
        }
    }

    private fun showDetailedStats(stats: com.example.mentalhealthapp.database.entities.UserStats) {
        val message = buildString {
            append("📊 ESTADÍSTICAS DETALLADAS\n\n")
            append("📝 DIARIO: ${stats.totalJournalEntries} entradas\n")
            append("🌬️ RESPIRACIÓN: ${stats.totalBreathingSessions} sesiones\n")
            append("🎮 BURBUJAS: ${stats.totalBubblesPopped} reventadas\n")
            append("🧠 MEMORIA: ${stats.memoryGamesPlayed} juegos\n")
            append("🎨 COLOR: ${stats.colorGamesPlayed} juegos")
        }

        AlertDialog.Builder(this)
            .setTitle("Tus Estadísticas")
            .setMessage(message)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Compartir") { _, _ -> shareStats(message) }
            .show()
    }

    private fun shareStats(stats: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$stats\n\n✨ MindCare App")
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir estadísticas"))
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro?")
            .setPositiveButton("Cerrar sesión") { _, _ -> logout() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE).edit().apply {
            remove("current_user_id")
            putBoolean("is_logged_in", false)
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupNotifications() {
        val scheduler = NotificationScheduler(this)
        scheduler.scheduleBreathingReminders()
        scheduler.scheduleJournalReminder()
        scheduler.scheduleMotivationalMessages()
    }

    override fun onResume() {
        super.onResume()
        showStats()
    }
}