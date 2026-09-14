package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.TypeConverters

@Database(
    entities = [ScorecardGameEntity::class, ScorecardRoundEntity::class, MatchHistoryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(MatchTypeConverters::class)
abstract class KaachuPhoolDatabase : RoomDatabase() {
    abstract fun scorecardDao(): ScorecardDao
    abstract fun matchHistoryDao(): MatchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: KaachuPhoolDatabase? = null

        fun getDatabase(context: Context): KaachuPhoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaachuPhoolDatabase::class.java,
                    "kaachu_phool_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
