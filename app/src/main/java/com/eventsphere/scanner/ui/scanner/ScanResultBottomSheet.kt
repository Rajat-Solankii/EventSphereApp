package com.eventsphere.scanner.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.databinding.BottomSheetScanResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScanResultBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetScanResultBinding? = null
    private val binding get() = _binding!!

    private var scanResult: ScanResult? = null
    private var onDismissListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        scanResult?.let { result ->
            binding.attendeeName.text = result.attendee?.name ?: result.ticketInfo?.attendeeName ?: "Unknown Attendee"
            binding.resultMessage.text = result.message
            
            // "Roll No" isn't explicitly in models, using ID or empty
            binding.rollNo.text = "ID: ${result.attendee?.id ?: result.ticketInfo?.ticketId ?: "N/A"}"

            if (result.success) {
                binding.resultIcon.setImageResource(R.drawable.ic_success)
                binding.resultTitle.text = "Access Granted"
                binding.resultTitle.setTextColor(requireContext().getColor(R.color.success))
            } else {
                binding.resultIcon.setImageResource(R.drawable.ic_error)
                binding.resultTitle.text = "Access Denied"
                binding.resultTitle.setTextColor(requireContext().getColor(R.color.error))
            }
        }

        binding.btnDismiss.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onDismissListener?.invoke()
    }

    companion object {
        const val TAG = "ScanResultBottomSheet"
        
        fun newInstance(result: ScanResult, onDismiss: () -> Unit): ScanResultBottomSheet {
            return ScanResultBottomSheet().apply {
                this.scanResult = result
                this.onDismissListener = onDismiss
            }
        }
    }
}
