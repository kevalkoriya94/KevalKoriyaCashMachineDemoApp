package com.example.cashmachinedemoapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ReportFragment
import androidx.lifecycle.ViewModelProvider
import com.example.cashmachinedemoapp.R
import com.example.cashmachinedemoapp.databinding.ActivityMainBinding
import com.example.cashmachinedemoapp.fragments.CreditDebitFragment
import com.example.cashmachinedemoapp.fragments.HistoryFragment
import com.example.cashmachinedemoapp.fragments.SummayFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    var binding: ActivityMainBinding? = null

    lateinit var viewmodel: CashMachineViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val factory = CashMachineViewmodel.Factory(application)

        viewmodel = ViewModelProvider(this,factory)[CashMachineViewmodel::class.java]


        loadFragment(CreditDebitFragment())
        binding?.bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.creddeb -> {
                    loadFragment(CreditDebitFragment())
                    true
                }
                R.id.history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.report -> {
                    loadFragment(SummayFragment())
                    true
                }
                else -> false
            }
        }
    }

    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.commit()
    }
}