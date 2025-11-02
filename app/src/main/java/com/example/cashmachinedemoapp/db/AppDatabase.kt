package com.example.cashmachinedemoapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.model.Transaction
import com.example.cashmachinedemoapp.model.TransactionConverters

@Database(entities = [Transaction::class, DenominationEntity::class], version = 1)
@TypeConverters(TransactionConverters::class)
abstract class AppDatabase: RoomDatabase()  {
    abstract fun transactionDao(): TransactionDao

    companion object{
        const val DATABASE_NAME = "transaction_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}