package com.example.cashmachinedemoapp.db

import androidx.lifecycle.LiveData
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.model.Transaction
import com.example.cashmachinedemoapp.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class CashMachineRepo(private val transactionDao: TransactionDao) {

    private val denominationList = listOf(500, 200, 100, 50, 20, 10)

    val denominations: LiveData<List<DenominationEntity>> = transactionDao.getAllDenomination()
    val transactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    init {
        initializeDenomination()
    }

    private fun initializeDenomination() {
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

    private suspend fun distributeAmount(targetAmount: Int): Map<Int, Int>? =
        withContext(Dispatchers.IO) {
            var remaining = targetAmount
            val distribution = mutableMapOf<Int, Int>()
            val currencyInventory = transactionDao.getAllDenominationEntity().associate {
                it.value to it.count
            }

            for (note in denominationList) {
                if (remaining == 0) break

                val availableCount = currencyInventory.getOrDefault(note, 0)
                val neededCount = remaining / note

                val countToUse = minOf(neededCount, availableCount)

                if (countToUse > 0) {
                    distribution[note] = countToUse
                    remaining -= countToUse * note
                }
            }

            return@withContext if (remaining == 0) distribution else null
        }

    private suspend fun calculateCreditDistribution(amount: Int): Map<Int, Int> {
        var remaining = amount
        val distribution = mutableMapOf<Int, Int>()

        for (note in denominationList) {
            if (remaining == 0) break
            val count = remaining / note
            if (count > 0) {
                distribution[note] = count
                remaining %= note
            }
        }

        return distribution
    }

    suspend fun creditAmount(amount: Int, denominationMap: Map<Int, Int>): String? = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext "Amount must be a positive number"
        if (amount % 10 != 0) return@withContext "Amount must be a multiple of 10"

        val currentDenomination = transactionDao.getAllDenominationEntity().associateBy {
            it.value
        }.toMutableMap()

        val denominationToUpdate = mutableListOf<DenominationEntity>()

        denominationMap.forEach { (note, count) ->
            if (count > 0) {
                val entity = currentDenomination[note] ?: DenominationEntity(note, 0)
                denominationToUpdate.add(entity.copy(count = entity.count + count))
            }
        }

        if (denominationToUpdate.isEmpty()) {
            return@withContext "No denominations provided"
        }

        transactionDao.update(denominationToUpdate)

        val transaction = Transaction(
            type = TransactionType.CREDIT,
            amount = amount,
            timestamp = Date().time,
            denominationBreakDown = denominationMap
        )

        transactionDao.insertTransaction(transaction)
        return@withContext null
    }

    suspend fun debitAmount(amount: Int, denominationMap: Map<Int, Int>): String? = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext "Amount must be a positive number"
        if (amount % 10 != 0) return@withContext "Amount must be a multiple of 10"

        val totalBalance = getTotalBalance()
        if (amount > totalBalance) return@withContext "Insufficient balance"

        val currentDenomination = transactionDao.getAllDenominationEntity().associateBy {
            it.value
        }.toMutableMap()

        val denominationToUpdate = mutableListOf<DenominationEntity>()
        val distribution: Map<Int, Int>

        // If denominationMap is provided, use it; otherwise distribute automatically
        if (denominationMap.isNotEmpty() && denominationMap.any { it.value > 0 }) {
            // Validate manual denomination input
            denominationMap.forEach { (note, count) ->
                if (count > 0) {
                    val entity = currentDenomination[note] ?: return@withContext "Denomination ₹$note not found"
                    if (entity.count < count) return@withContext "Insufficient ₹$note notes. Available: ${entity.count}, Requested: $count"
                    denominationToUpdate.add(entity.copy(count = entity.count - count))
                }
            }
            distribution = denominationMap
        } else {
            // Automatic distribution
            val autoDistribution = distributeAmount(amount) ?: return@withContext "Cannot distribute amount with available denominations"

            autoDistribution.forEach { (note, count) ->
                val entity = currentDenomination[note] ?: return@withContext "Denomination ₹$note not found"
                denominationToUpdate.add(entity.copy(count = entity.count - count))
            }
            distribution = autoDistribution
        }

        if (denominationToUpdate.isEmpty()) {
            return@withContext "No denominations available for distribution"
        }

        transactionDao.update(denominationToUpdate)

        val transaction = Transaction(
            type = TransactionType.DEBIT,
            amount = amount,
            timestamp = Date().time,
            denominationBreakDown = distribution
        )

        transactionDao.insertTransaction(transaction)
        return@withContext null
    }
}