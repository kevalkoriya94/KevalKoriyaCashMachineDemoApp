package com.example.cashmachinedemoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cashmachinedemoapp.databinding.FragmentCreditDebitBinding
import com.example.cashmachinedemoapp.model.TranscationType
import com.example.cashmachinedemoapp.ui.CashMachineViewmodel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreditDebitFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreditDebitFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var bind: FragmentCreditDebitBinding? = null
    lateinit var viewmodel: CashMachineViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentCreditDebitBinding.inflate(inflater, container, false)
        return bind!!.root
    }

    var curruntType = TranscationType.CREDIT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind?.btnCredit?.setOnClickListener {
            bind?.edtDebitAmount?.visibility = View.GONE
            bind?.creditView?.visibility = View.VISIBLE
            setTransactionType(TranscationType.CREDIT)
        }

        bind?.btndebit?.setOnClickListener {
            bind?.edtDebitAmount?.visibility = View.VISIBLE
            bind?.creditView?.visibility = View.GONE
            setTransactionType(TranscationType.DEBIT)
        }

        bind?.btnSubmit?.setOnClickListener {
            handleCreditSubmit()
        }

    }

    private fun handleCreditSubmit() {
        val amountText = bind?.edit100?.text.toString()
        val amount = amountText.toIntOrNull()

        if (amount == null || amount <= 0) {
            bind?.edtDebitAmount?.error = "Invalid amount"
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount % 10 != 0) {
            Toast.makeText(requireContext(), "Amount not multiple of 10", Toast.LENGTH_SHORT).show()
            return
        }

        bind?.btnSubmit?.isEnabled = false

        val operation: (Int, (String?) -> Unit) -> Unit = if(curruntType == TranscationType.CREDIT){
            viewmodel::creditAmount
        }else{
            viewmodel::debitAmount
        }
        operation(amount){
            errorMessage ->
            bind?.btnSubmit?.isEnabled = true
            if(errorMessage != null){
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun setTransactionType(type: TranscationType){
        curruntType = type

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreditDebitFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreditDebitFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}