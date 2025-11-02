package com.example.cashmachinedemoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "Transaction")
@TypeConverters(TransactionConverters::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val denominationBreakDown: Map<Int, Int>
)

enum class TransactionType {
    CREDIT, DEBIT
}