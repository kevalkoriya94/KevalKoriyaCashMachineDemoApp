package com.example.cashmachinedemoapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.model.Transaction

@Dao
interface  TransactionDao {
    @Insert
    fun insertAllDenomination(denominationEntity: List<DenominationEntity>)

    @Query("SELECT * FROM denomination ORDER BY value DESC")
    fun getAllDenomination(): LiveData<List<DenominationEntity>>

    @Query("SELECT * FROM denomination")
    fun getAllDenominationEntity(): List<DenominationEntity>

    @Update
    suspend fun update(denominationEntity: List<DenominationEntity>)

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM `transaction` ORDER BY timestamp DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

}