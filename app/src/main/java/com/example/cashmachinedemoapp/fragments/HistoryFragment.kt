package com.example.cashmachinedemoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashmachinedemoapp.databinding.FragmentHistoryBinding
import com.example.cashmachinedemoapp.databinding.ItemTransactionBinding
import com.example.cashmachinedemoapp.model.Transaction
import com.example.cashmachinedemoapp.ui.CashMachineViewmodel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CashMachineViewmodel by activityViewModels()
    private val adapter = TransactionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter
    }

    private fun observeTransactions() {
        viewModel.transactionEntries.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            if (transactions.isEmpty()) {
                binding.txtEmptyHistory.visibility = View.VISIBLE
                binding.recyclerViewHistory.visibility = View.GONE
            } else {
                binding.txtEmptyHistory.visibility = View.GONE
                binding.recyclerViewHistory.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions = emptyList<Transaction>()

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.txtType.text = transaction.type.name
            binding.txtAmount.text = "₹${transaction.amount}"
            binding.txtDate.text = formatDate(transaction.timestamp)
            binding.txtBreakdown.text = formatBreakdown(transaction.denominationBreakDown)

            // Set color based on transaction type
            val color = if (transaction.type.name == "CREDIT") {
                android.graphics.Color.GREEN
            } else {
                android.graphics.Color.RED
            }
            binding.txtType.setTextColor(color)
        }

        private fun formatDate(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            return format.format(date)
        }

        private fun formatBreakdown(breakdown: Map<Int, Int>): String {
            return breakdown.entries
                .filter { it.value > 0 }
                .sortedByDescending { it.key }
                .joinToString(", ") { "₹${it.key} × ${it.value}" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    fun submitList(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}