package com.example.mentalhealthapp.database.dao

import androidx.room.*
import com.example.mentalhealthapp.database.entities.UserStats

@Dao
interface UserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: UserStats)

    @Update
    suspend fun updateStats(stats: UserStats)

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    suspend fun getStats(userId: Int): UserStats?

    @Query("UPDATE user_stats SET totalBubblesPopped = totalBubblesPopped + :amount WHERE userId = :userId")
    suspend fun incrementBubblesPopped(userId: Int, amount: Int)

    @Query("UPDATE user_stats SET bubbleHighScore = :score WHERE userId = :userId AND :score > bubbleHighScore")
    suspend fun updateBubbleHighScore(userId: Int, score: Int)

    @Query("UPDATE user_stats SET memoryGamesPlayed = memoryGamesPlayed + 1 WHERE userId = :userId")
    suspend fun incrementMemoryGamesPlayed(userId: Int)

    @Query("UPDATE user_stats SET memoryBestScore = :score WHERE userId = :userId AND :score < memoryBestScore")
    suspend fun updateMemoryBestScore(userId: Int, score: Int)

    @Query("UPDATE user_stats SET colorGamesPlayed = colorGamesPlayed + 1 WHERE userId = :userId")
    suspend fun incrementColorGamesPlayed(userId: Int)

    @Query("UPDATE user_stats SET colorHighScore = :score WHERE userId = :userId AND :score > colorHighScore")
    suspend fun updateColorHighScore(userId: Int, score: Int)

    @Query("UPDATE user_stats SET breathingCycles = breathingCycles + :cycles, totalBreathingSessions = totalBreathingSessions + 1 WHERE userId = :userId")
    suspend fun updateBreathingStats(userId: Int, cycles: Int)

    @Query("UPDATE user_stats SET totalJournalEntries = totalJournalEntries + 1 WHERE userId = :userId")
    suspend fun incrementJournalEntries(userId: Int)

    @Query("UPDATE user_stats SET achievementFirstEntry = 1 WHERE userId = :userId")
    suspend fun unlockFirstEntry(userId: Int)

    @Query("UPDATE user_stats SET achievementTenEntries = 1 WHERE userId = :userId")
    suspend fun unlockTenEntries(userId: Int)

    @Query("UPDATE user_stats SET achievementBubbleMaster = 1 WHERE userId = :userId")
    suspend fun unlockBubbleMaster(userId: Int)

    @Query("UPDATE user_stats SET achievementBreathingZen = 1 WHERE userId = :userId")
    suspend fun unlockBreathingZen(userId: Int)
}