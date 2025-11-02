package com.example.cashmachinedemoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cashmachinedemoapp.db.AppDatabase
import com.example.cashmachinedemoapp.db.CashMachineRepo
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.model.Transaction
import kotlinx.coroutines.launch

class CashMachineViewmodel(application: Application) : AndroidViewModel(application) {
    private val repo: CashMachineRepo

    private val _totalBalance = MutableLiveData(0)
    val totalBalance: LiveData<Int> = _totalBalance

    val denominationEntries: LiveData<List<DenominationEntity>>
    val transactionEntries: LiveData<List<Transaction>>

    init {
        val dao = AppDatabase.getInstance(application).transactionDao()
        repo = CashMachineRepo(dao)
        denominationEntries = repo.denominations
        transactionEntries = repo.transactions

        viewModelScope.launch {
            updateTotalBalance()
        }
    }

    private suspend fun updateTotalBalance() {
        _totalBalance.postValue(repo.getTotalBalance())
    }

    fun creditAmount(amount: Int, denominationMap: Map<Int, Int>, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repo.creditAmount(amount, denominationMap)
            updateTotalBalance()
            callback(result)
        }
    }

    fun debitAmount(amount: Int, denominationMap: Map<Int, Int>, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repo.debitAmount(amount, denominationMap)
            updateTotalBalance()
            callback(result)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CashMachineViewmodel::class.java)) {
                return CashMachineViewmodel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}