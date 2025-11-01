package com.example.cashmachinedemoapp.db

import androidx.lifecycle.LiveData
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.model.Transaction
import com.example.cashmachinedemoapp.model.TranscationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class CashMachineRepo(private val transactionDao: TransactionDao) {

    private val denominationList = listOf(500, 200, 100, 50, 20, 10)

    val denominations: LiveData<List<DenominationEntity>> = transactionDao.getAllDenomination()
    val transactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    init {
        initialiseDenomination()
    }

    private fun initialiseDenomination() {
        kotlin.runCatching {
            Thread {
                val existing = transactionDao.getAllDenominationEntity()
                if (existing.isEmpty()) {
                    val initialList = denominationList.map {
                        DenominationEntity(it, 0)
                    }

                    transactionDao.insertAllDenomination(initialList)
                }
            }.start()
        }
    }

    suspend fun getTotalBalance(): Int = withContext(Dispatchers.IO) {
        transactionDao.getAllDenominationEntity().sumOf {
            it.value * it.count
        }
    }

    private suspend fun distributG(targetAmount: Int): Map<Int, Int>? =
        withContext(Dispatchers.IO) {
            var remaining = targetAmount
            val distribution = mutableMapOf<Int, Int>()
            val curruncyInventory = transactionDao.getAllDenominationEntity().associate {
                it.value to it.count
            }

            for (note in denominationList) {
                if (remaining == 0) {
                    break
                }

                val availableCount = curruncyInventory.getOrDefault(note, 0)
                val neededCount = remaining / note

                val Counttouse = minOf(neededCount, availableCount)

                if (Counttouse > 0) {
                    distribution[note] = Counttouse
                    remaining -= Counttouse * note
                }

                return@withContext if
                                           (remaining == 0) distribution else null
            }

        } as Map<Int, Int>?

    suspend fun creditAmount(amount: Int): String? = withContext(Dispatchers.IO) {
        if (amount <= 0)
            return@withContext "Amount must be a positive number"


        if (amount % 10 != 0)
            return@withContext "Amount must be a multiple of 10"

        val distribution = distributG(amount) ?: return@withContext "Internal error in distribution"

        val curruntDenomination = transactionDao.getAllDenominationEntity().associateBy {
            it.value
        }.toMutableMap()

        val denominationtoUpdate = mutableListOf<DenominationEntity>()

        distribution.forEach { note, count ->
            val entity = curruntDenomination[note] ?: DenominationEntity(note, 0)

            denominationtoUpdate.add(entity.copy(count = entity.count + count))
        }

        transactionDao.update(denominationtoUpdate)
        val trasaction = Transaction(
            type = TranscationType.CREDIT,
            amount = amount,
            timestamp = Date().time, denominationBreakDown = distribution
        )


        transactionDao.insertTransaction(trasaction)
        return@withContext null
    }

}