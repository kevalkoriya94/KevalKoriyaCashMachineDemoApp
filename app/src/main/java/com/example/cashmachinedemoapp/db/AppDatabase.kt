package com.example.cashmachinedemoapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cashmachinedemoapp.model.Transaction

@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase: RoomDatabase()  {
    abstract fun transitionDao(): TransactionDao

    companion object{
        const val DATABASE_NAME = "transaction_db"
        var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            synchronized(this) {
                val newInstance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                    DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = newInstance

                return newInstance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_detail ADD COLUMN emailId TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}