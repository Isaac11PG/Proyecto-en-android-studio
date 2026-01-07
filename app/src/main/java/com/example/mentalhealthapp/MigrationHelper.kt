package com.example.mentalhealthapp

import android.content.Context
import com.example.mentalhealthapp.database.AppDatabase
import com.example.mentalhealthapp.database.entities.JournalEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MigrationHelper(private val context: Context) {

    fun migrateSharedPreferencesToRoom(userId: Int) {
        val prefs = context.getSharedPreferences("MindCarePrefs", Context.MODE_PRIVATE)
        val database = AppDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            // Migrar entradas del diario
            val entries = prefs.getStringSet("journal_entries", emptySet()) ?: emptySet()

            for (entry in entries) {
                val parts = entry.split("|")
                if (parts.size >= 3) {
                    val timestamp = parts[0].toLong()
                    val title = parts[1]
                    val content = parts[2]
                    val mood = if (parts.size > 3) parts[3] else "Sin registro"

                    val journalEntry = JournalEntry(
                        userId = userId,
                        title = title,
                        content = content,
                        mood = mood,
                        timestamp = timestamp
                    )

                    database.journalDao().insertEntry(journalEntry)
                }
            }

            // Migrar estadísticas si existen
            val stats = database.userStatsDao().getStats(userId)
            if (stats != null) {
                val updatedStats = stats.copy(
                    totalBubblesPopped = prefs.getInt("total_bubbles_popped", 0),
                    breathingCycles = prefs.getInt("breathing_cycles", 0),
                    totalJournalEntries = prefs.getInt("total_journal_entries", 0)
                )
                database.userStatsDao().updateStats(updatedStats)
            }

            // Marcar migración como completada
            prefs.edit().putBoolean("migration_completed", true).apply()
        }
    }
}