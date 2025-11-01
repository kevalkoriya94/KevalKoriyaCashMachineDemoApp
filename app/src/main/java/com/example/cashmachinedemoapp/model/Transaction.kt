package com.example.cashmachinedemoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TranscationType,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val denominationBreakDown: Map<Int, Int>
)


enum class TranscationType{
    CREDIT,DEBIT
}