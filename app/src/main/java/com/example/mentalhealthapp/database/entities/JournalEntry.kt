package com.example.mentalhealthapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val title: String,
    val content: String,
    val mood: String,
    val timestamp: Long = System.currentTimeMillis()
)