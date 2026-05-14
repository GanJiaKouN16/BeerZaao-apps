package com.example.beerzaao.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FundEntity::class], version = 1, exportSchema = false)
abstract class FundDatabase : RoomDatabase() {
    abstract fun fundDao(): FundDao

    companion object {
        @Volatile
        private var INSTANCE: FundDatabase? = null

        fun getDatabase(context: Context): FundDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FundDatabase::class.java,
                    "fund_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
