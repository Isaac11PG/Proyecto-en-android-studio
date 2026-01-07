package com.example.mentalhealthapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mentalhealthapp.database.dao.JournalDao
import com.example.mentalhealthapp.database.dao.UserDao
import com.example.mentalhealthapp.database.dao.UserStatsDao
import com.example.mentalhealthapp.database.entities.JournalEntry
import com.example.mentalhealthapp.database.entities.User
import com.example.mentalhealthapp.database.entities.UserStats

@Database(
    entities = [User::class, JournalEntry::class, UserStats::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindcare_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}