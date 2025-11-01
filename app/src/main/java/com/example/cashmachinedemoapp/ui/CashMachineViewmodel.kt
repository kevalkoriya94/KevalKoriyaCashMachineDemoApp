package com.example.cashmachinedemoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cashmachinedemoapp.db.AppDatabase
import com.example.cashmachinedemoapp.db.CashMachineRepo
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.model.Transaction
import kotlinx.coroutines.launch

class CashMachineViewmodel(application: Application) : ViewModel() {
    private val repo: CashMachineRepo

    val _totalBalance = MutableLiveData(0)
    val totalBalance: LiveData<Int> = _totalBalance

    val denominationEntries: LiveData<List<DenominationEntity>>
    val transactionEntries: LiveData<List<Transaction>>

    init {
        val dao = AppDatabase.getInstance(application).transitionDao()
        repo = CashMachineRepo(dao)
        denominationEntries = repo.denominations
        transactionEntries = repo.transactions

        denominationEntries.observeForever{
            entities ->
            _totalBalance.value = entities.sumOf { it.value * it.count }
        }
    }

    fun creditAmount(amount: Int, callback: (String?) -> Unit){
        viewModelScope.launch {
            val result = repo.creditAmount(amount)
            callback(result)
        }
    }

    fun debitAmount(amount: Int, callback: (String?) -> Unit){
        viewModelScope.launch {
            val result = repo.creditAmount(amount)
            callback(result)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory
    {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CashMachineViewmodel::class.java)) {
                return CashMachineViewmodel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}