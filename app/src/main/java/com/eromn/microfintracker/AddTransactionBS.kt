package com.eromn.microfintracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.eromn.microfintracker.databinding.BsAddTransactionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddTransactionBS : BottomSheetDialogFragment() {
    // Use _binding pattern for safety against memory leaks in fragments
    private var _binding: BsAddTransactionBinding?= null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // 1. Define an interface for communication
    interface TransactionDetailsListener {
        fun onTransactionDetailsEntered(description: String, amount: Double)
    }

    private var listener: TransactionDetailsListener? = null

    // 2. Method to set the listener from the calling Activity/Fragment
    fun setTransactionDetailsListener(listener: TransactionDetailsListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BsAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveTransaction.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()
            val amountString = binding.etAmount.text.toString().trim()

            var isValid = true // Flag to track overall validity

            if (description.isBlank()) {
                binding.tilDescription.error = "Falta una descripción, subnormal!"
                isValid = false
            } else {
                binding.tilDescription.error = null // Clear error
            }

            if (amountString.isBlank()) {
                binding.tilAmount.error = "Falta un monto, subnormal!"
                isValid = false
            } else {
                val amount = amountString.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    binding.tilAmount.error = "Monto inválido, subnormal!"
                    isValid = false
                } else {
                    binding.tilAmount.error = null // Clear error
                }
            }// end if-else amount

            if (isValid) {
                val finalAmount = amountString.toDouble()
                listener?.onTransactionDetailsEntered(description, finalAmount)
                Toast.makeText(requireContext(), "¡Viaje agregado!", Toast.LENGTH_SHORT).show()
                dismiss()
            }// end if-else isValid
        }// end btnSaveTransaction

        binding.btnCancelTransaction.setOnClickListener {
            dismiss()
        }
    }// end onViewCreated

    // 5. (Optional but good practice) Companion object for easy instance creation and TAG
    companion object {
        const val TAG = "AddTransactionBS"
        fun newInstance(): AddTransactionBS {
            return AddTransactionBS()
        }
    }// end companion object

    // 6. (Important for listener) Ensure the calling context implements the listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TransactionDetailsListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnAddTransactionListener")
        }
    }// end onAttach

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}// end class AddTransactionBS