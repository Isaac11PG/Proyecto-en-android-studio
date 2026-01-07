package com.example.mentalhealthapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.mentalhealthapp.database.AppDatabase
import com.example.mentalhealthapp.database.entities.JournalEntry
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JournalActivity : AppCompatActivity() {

    private lateinit var containerEntries: LinearLayout
    private lateinit var buttonNewEntry: Button
    private lateinit var rootView: android.view.View
    private lateinit var database: AppDatabase

    private var currentMood: String? = null
    private var currentUserId: Int = -1
    private var entries: List<JournalEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mi Diario Personal"

        rootView = findViewById(android.R.id.content)
        containerEntries = findViewById(R.id.containerEntries)
        buttonNewEntry = findViewById(R.id.buttonNewEntry)

        database = AppDatabase.getDatabase(this)

        // Obtener ID del usuario actual
        val sharedPreferences = getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            Snackbar.make(rootView, "Error: Usuario no identificado", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        // Recibir estado de ánimo si viene de MainActivity
        currentMood = intent.getStringExtra("mood")
        currentMood?.let {
            Snackbar.make(rootView, "Estado actual: $it", Snackbar.LENGTH_LONG).show()
        }

        buttonNewEntry.setOnClickListener {
            showNewEntryDialog()
        }

        loadEntries()

        // Mostrar consejo si es primera vez
        if (!sharedPreferences.getBoolean("journal_tutorial_shown", false)) {
            Snackbar.make(
                rootView,
                "💡 Escribir tus pensamientos ayuda a reducir el estrés",
                Snackbar.LENGTH_LONG
            ).setAction("Entendido") {
                sharedPreferences.edit().putBoolean("journal_tutorial_shown", true).apply()
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }

    private fun showNewEntryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_journal_entry, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editEntryTitle)
        val editContent = dialogView.findViewById<EditText>(R.id.editEntryContent)

        // Pre-llenar con estado de ánimo si existe
        currentMood?.let {
            editTitle.setText("Hoy me siento $it")
        }

        AlertDialog.Builder(this)
            .setTitle("Nueva Entrada")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val title = editTitle.text.toString().trim()
                val content = editContent.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    saveEntry(title, content)
                } else {
                    Snackbar.make(rootView, "Completa todos los campos", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveEntry(title: String, content: String) {
        lifecycleScope.launch {
            val mood = currentMood ?: "Sin registro"

            val entry = JournalEntry(
                userId = currentUserId,
                title = title,
                content = content,
                mood = mood
            )

            database.journalDao().insertEntry(entry)

            // Actualizar estadísticas
            database.userStatsDao().incrementJournalEntries(currentUserId)

            // Verificar logros
            val stats = database.userStatsDao().getStats(currentUserId)
            stats?.let {
                if (it.totalJournalEntries == 1 && !it.achievementFirstEntry) {
                    database.userStatsDao().unlockFirstEntry(currentUserId)
                    NotificationHelper(this@JournalActivity).sendAchievementNotification(
                        "Primer Paso",
                        "¡Has escrito tu primera entrada en el diario!"
                    )
                }

                if (it.totalJournalEntries == 10 && !it.achievementTenEntries) {
                    database.userStatsDao().unlockTenEntries(currentUserId)
                    NotificationHelper(this@JournalActivity).sendAchievementNotification(
                        "Escritor Consistente",
                        "¡Has completado 10 entradas en tu diario!"
                    )
                }
            }

            Snackbar.make(rootView, "✓ Entrada guardada", Snackbar.LENGTH_SHORT)
                .setAction("Ver") {
                    loadEntries()
                }.show()

            loadEntries()
            currentMood = null
        }
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            entries = database.journalDao().getAllEntries(currentUserId)
            displayEntries()
        }
    }

    private fun displayEntries() {
        containerEntries.removeAllViews()

        if (entries.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "📝 Aún no tienes entradas.\n¡Comienza a escribir tu historia!"
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(32, 64, 32, 64)
            }
            containerEntries.addView(emptyText)
            return
        }

        for (entry in entries) {
            addEntryCard(entry)
        }
    }

    private fun addEntryCard(entry: JournalEntry) {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 16f
            cardElevation = 8f
            setCardBackgroundColor(resources.getColor(R.color.white, null))
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "ES"))
        val date = dateFormat.format(Date(entry.timestamp))

        val dateText = TextView(this).apply {
            text = "📅 $date"
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        val moodText = TextView(this).apply {
            text = "Estado: ${entry.mood}"
            textSize = 14f
            setTextColor(resources.getColor(R.color.primary, null))
            setPadding(0, 4, 0, 4)
        }

        val titleText = TextView(this).apply {
            text = entry.title
            textSize = 20f
            setTextColor(resources.getColor(R.color.text_primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }

        val contentText = TextView(this).apply {
            text = entry.content
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_primary, null))
            maxLines = 3
        }

        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }

        val shareButton = Button(this).apply {
            text = "📤 Compartir"
            setBackgroundColor(resources.getColor(R.color.accent, null))
            setTextColor(resources.getColor(R.color.white, null))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = 8
            }
            setOnClickListener {
                shareEntry(entry, date)
            }
        }

        val deleteButton = Button(this).apply {
            text = "🗑️ Eliminar"
            setBackgroundColor(resources.getColor(android.R.color.holo_red_light, null))
            setTextColor(resources.getColor(R.color.white, null))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 8
            }
            setOnClickListener {
                deleteEntry(entry)
            }
        }

        buttonContainer.addView(shareButton)
        buttonContainer.addView(deleteButton)

        layout.addView(dateText)
        layout.addView(moodText)
        layout.addView(titleText)
        layout.addView(contentText)
        layout.addView(buttonContainer)
        card.addView(layout)
        containerEntries.addView(card)
    }

    private fun shareEntry(entry: JournalEntry, date: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Mi Diario - ${entry.title}")
            putExtra(Intent.EXTRA_TEXT, "📝 ${entry.title}\n📅 $date\n\n${entry.content}\n\n✨ Escrito en MindCare")
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir entrada"))
    }

    private fun deleteEntry(entry: JournalEntry) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar entrada")
            .setMessage("¿Estás seguro de eliminar esta entrada?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    database.journalDao().deleteEntry(entry)

                    Snackbar.make(rootView, "Entrada eliminada", Snackbar.LENGTH_LONG)
                        .setAction("Deshacer") {
                            lifecycleScope.launch {
                                database.journalDao().insertEntry(entry)
                                loadEntries()
                            }
                        }.show()

                    loadEntries()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}