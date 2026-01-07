package com.example.mentalhealthapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "user_stats",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserStats(
    @PrimaryKey
    val userId: Int,

    // Estadísticas de juegos
    val totalBubblesPopped: Int = 0,
    val bubbleHighScore: Int = 0,
    val memoryGamesPlayed: Int = 0,
    val memoryBestScore: Int = 999,
    val colorGamesPlayed: Int = 0,
    val colorHighScore: Int = 0,

    // Estadísticas de ejercicios
    val breathingCycles: Int = 0,
    val totalBreathingSessions: Int = 0,

    // Estadísticas de diario
    val totalJournalEntries: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,

    // Logros
    val achievementFirstEntry: Boolean = false,
    val achievementTenEntries: Boolean = false,
    val achievementBubbleMaster: Boolean = false,
    val achievementBreathingZen: Boolean = false
)