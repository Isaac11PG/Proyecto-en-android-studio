package com.example.mentalhealthapp.database.dao

import androidx.room.*
import com.example.mentalhealthapp.database.entities.JournalEntry

@Dao
interface JournalDao {

    @Insert
    suspend fun insertEntry(entry: JournalEntry): Long

    @Update
    suspend fun updateEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllEntries(userId: Int): List<JournalEntry>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND id = :entryId LIMIT 1")
    suspend fun getEntryById(userId: Int, entryId: Int): JournalEntry?

    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId")
    suspend fun getEntryCount(userId: Int): Int

    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun deleteAllEntries(userId: Int)

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getEntriesSince(userId: Int, startTime: Long): List<JournalEntry>
}