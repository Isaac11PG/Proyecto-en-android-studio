package com.example.mentalhealthapp.database.dao

import androidx.room.*
import com.example.mentalhealthapp.database.entities.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE users SET lastMood = :mood WHERE id = :userId")
    suspend fun updateMood(userId: Int, mood: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}