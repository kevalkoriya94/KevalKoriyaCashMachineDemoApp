package com.example.cashmachinedemoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashmachinedemoapp.databinding.FragmentSummayBinding
import com.example.cashmachinedemoapp.databinding.ItemDenominationBinding
import com.example.cashmachinedemoapp.model.DenominationEntity
import com.example.cashmachinedemoapp.ui.CashMachineViewmodel

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CashMachineViewmodel by activityViewModels()
    private val adapter = DenominationAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDenominations.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDenominations.adapter = adapter
    }

    private fun observeData() {
        viewModel.denominationEntries.observe(viewLifecycleOwner) { denominations ->
            adapter.submitList(denominations)
        }

        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            binding.txtTotalBalance.text = "₹$balance"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = SummaryFragment()
    }
}

class DenominationAdapter : RecyclerView.Adapter<DenominationAdapter.DenominationViewHolder>() {

    private var denominations = emptyList<DenominationEntity>()

    class DenominationViewHolder(private val binding: ItemDenominationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(denomination: DenominationEntity) {
            binding.txtDenomination.text = "₹${denomination.value}"
            binding.txtCount.text = "Count: ${denomination.count}"
            binding.txtSubtotal.text = "Subtotal: ₹${denomination.value * denomination.count}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenominationViewHolder {
        val binding = ItemDenominationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DenominationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DenominationViewHolder, position: Int) {
        holder.bind(denominations[position])
    }

    override fun getItemCount() = denominations.size

    fun submitList(newList: List<DenominationEntity>) {
        denominations = newList.sortedByDescending { it.value }
        notifyDataSetChanged()
    }
}