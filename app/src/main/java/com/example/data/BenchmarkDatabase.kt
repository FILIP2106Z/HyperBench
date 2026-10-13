package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BenchmarkEntity::class, UserProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BenchmarkDatabase : RoomDatabase() {

    abstract fun benchmarkDao(): BenchmarkDao

    companion object {
        @Volatile
        private var INSTANCE: BenchmarkDatabase? = null

        fun getInstance(context: Context): BenchmarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BenchmarkDatabase::class.java,
                    "hyperbench_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
