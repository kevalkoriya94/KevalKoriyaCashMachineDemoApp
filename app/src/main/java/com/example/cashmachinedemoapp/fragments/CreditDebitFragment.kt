package com.example.cashmachinedemoapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cashmachinedemoapp.databinding.FragmentCreditDebitBinding
import com.example.cashmachinedemoapp.model.TransactionType
import com.example.cashmachinedemoapp.ui.CashMachineViewmodel

/**
 * A simple [Fragment] subclass.
 * Use the [CreditDebitFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreditDebitFragment : Fragment() {
    private var _binding: FragmentCreditDebitBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CashMachineViewmodel

    private var currentType = TransactionType.CREDIT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditDebitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[CashMachineViewmodel::class.java]

        setupUI()
        setupListeners()
        setupTextWatchers()
    }

    private fun setupUI() {
        setTransactionType(TransactionType.CREDIT)
        updateTotalPreview()
    }

    private fun setupListeners() {
        binding.btnCredit.setOnClickListener {
            setTransactionType(TransactionType.CREDIT)
        }

        binding.btndebit.setOnClickListener {
            setTransactionType(TransactionType.DEBIT)
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmit()
        }
    }

    private fun setupTextWatchers() {
        // Add text watchers to all denomination EditTexts
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTotalPreview()
            }
        }

        binding.edt500.addTextChangedListener(textWatcher)
        binding.edt200.addTextChangedListener(textWatcher)
        binding.edt100.addTextChangedListener(textWatcher)
        binding.edt50.addTextChangedListener(textWatcher)
        binding.edt20.addTextChangedListener(textWatcher)
        binding.edt10.addTextChangedListener(textWatcher)
    }

    private fun setTransactionType(type: TransactionType) {
        currentType = type

        // Update button states
        if (type == TransactionType.CREDIT) {
            binding.btnCredit.setBackgroundColor(requireContext().getColor(android.R.color.holo_green_light))
            binding.btndebit.setBackgroundColor(requireContext().getColor(android.R.color.darker_gray))
            binding.amountInputLayout.visibility = View.GONE
        } else {
            binding.btndebit.setBackgroundColor(requireContext().getColor(android.R.color.holo_red_light))
            binding.btnCredit.setBackgroundColor(requireContext().getColor(android.R.color.darker_gray))
            binding.amountInputLayout.visibility = View.VISIBLE
        }
    }

    private fun updateTotalPreview() {
        val total = calculateTotalFromDenominations()
        binding.txtTotalPreview.text = "Total: ₹$total"

        // Auto-fill the amount field
        binding.edtAmount.setText(total.toString())
    }

    private fun calculateTotalFromDenominations(): Int {
        val count500 = binding.edt500.text.toString().toIntOrNull() ?: 0
        val count200 = binding.edt200.text.toString().toIntOrNull() ?: 0
        val count100 = binding.edt100.text.toString().toIntOrNull() ?: 0
        val count50 = binding.edt50.text.toString().toIntOrNull() ?: 0
        val count20 = binding.edt20.text.toString().toIntOrNull() ?: 0
        val count10 = binding.edt10.text.toString().toIntOrNull() ?: 0

        return (count500 * 500) + (count200 * 200) + (count100 * 100) +
                (count50 * 50) + (count20 * 20) + (count10 * 10)
    }

    private fun getDenominationMap(): Map<Int, Int> {
        return mapOf(
            500 to (binding.edt500.text.toString().toIntOrNull() ?: 0),
            200 to (binding.edt200.text.toString().toIntOrNull() ?: 0),
            100 to (binding.edt100.text.toString().toIntOrNull() ?: 0),
            50 to (binding.edt50.text.toString().toIntOrNull() ?: 0),
            20 to (binding.edt20.text.toString().toIntOrNull() ?: 0),
            10 to (binding.edt10.text.toString().toIntOrNull() ?: 0)
        )
    }

    private fun handleSubmit() {
        val amount = binding.edtAmount.text.toString().toIntOrNull()
        val denominationMap = getDenominationMap()

        // Validate amount
        if (amount == null || amount <= 0) {
            showError("Please enter a valid amount")
            return
        }

        if (amount % 10 != 0) {
            showError("Amount must be a multiple of 10")
            return
        }

        // Validate denominations match the amount
        val calculatedTotal = calculateTotalFromDenominations()
        if (calculatedTotal != amount) {
            showError("Denominations total (₹$calculatedTotal) doesn't match entered amount (₹$amount)")
            return
        }

        // Validate at least one denomination is provided
        if (denominationMap.all { it.value == 0 }) {
            showError("Please enter at least one denomination")
            return
        }

        binding.btnSubmit.isEnabled = false
        binding.txtError.visibility = View.GONE

        if (currentType == TransactionType.CREDIT) {
            handleCredit(amount, denominationMap)
        } else {
            handleDebit(amount, denominationMap)
        }
    }

    private fun handleCredit(amount: Int, denominationMap: Map<Int, Int>) {
        viewModel.creditAmount(amount, denominationMap) { errorMessage ->
            binding.btnSubmit.isEnabled = true
            if (errorMessage != null) {
                showError(errorMessage)
            } else {
                showSuccess("Credit transaction completed successfully")
                resetForm()
            }
        }
    }

    private fun handleDebit(amount: Int, denominationMap: Map<Int, Int>) {
        viewModel.debitAmount(amount, denominationMap) { errorMessage ->
            binding.btnSubmit.isEnabled = true
            if (errorMessage != null) {
                showError(errorMessage)
            } else {
                showSuccess("Debit transaction completed successfully")
                resetForm()
            }
        }
    }

    private fun resetForm() {
        binding.edtAmount.text?.clear()
        binding.edt500.setText("0")
        binding.edt200.setText("0")
        binding.edt100.setText("0")
        binding.edt50.setText("0")
        binding.edt20.setText("0")
        binding.edt10.setText("0")
        binding.txtTotalPreview.text = "Total: ₹0"
    }

    private fun showError(message: String) {
        binding.txtError.text = message
        binding.txtError.visibility = View.VISIBLE
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.txtError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreditDebitFragment()
    }
}
