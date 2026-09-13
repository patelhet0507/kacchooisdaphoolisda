package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScorecardGameEntity::class, ScorecardRoundEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KaachuPhoolDatabase : RoomDatabase() {
    abstract fun scorecardDao(): ScorecardDao

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
