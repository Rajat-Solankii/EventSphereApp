package com.eventsphere.scanner.ui.scanner

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.databinding.BottomSheetScanResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScanResultBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetScanResultBinding? = null
    private val binding get() = _binding!!

    private var scanResult: ScanResult? = null
    private var isScanMode: Boolean = true
    private var onDismissListener: (() -> Unit)? = null
    private var onTempExitListener: ((String) -> Unit)? = null
    private var onReEnterListener: ((String) -> Unit)? = null

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

    private fun showFullScreenImage(bitmap: Bitmap) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val imageView = ZoomableImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setImageBitmap(bitmap)
            setOnClickListener { dialog.dismiss() }
        }
        dialog.setContentView(imageView)
        dialog.show()
    }

    private fun setupUI() {
        scanResult?.let { result ->
            scanResult?.event?.let { eventBasic ->
                binding.layoutEventInfo.visibility = View.VISIBLE
                binding.dividerEventInfo.visibility = View.VISIBLE
                binding.tvEventTitle.text = eventBasic.title
                
                // Set the event image if available
                if (!eventBasic.imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(eventBasic.imageUrl)
                        .placeholder(R.drawable.ic_logo)
                        .into(binding.ivEventImage)
                } else {
                    binding.ivEventImage.setImageResource(R.drawable.ic_logo)
                }
            } ?: run {
                binding.layoutEventInfo.visibility = View.GONE
                binding.dividerEventInfo.visibility = View.GONE
            }

            val ticketId = result.attendee?.id ?: result.ticketInfo?.ticketId ?: ""
            binding.resultMessage.text = result.message

            if (result.attendee == null && result.ticketInfo == null) {
                binding.cvAttendeeInfo.visibility = View.GONE
            } else {
                binding.cvAttendeeInfo.visibility = View.VISIBLE
                binding.attendeeName.text = result.attendee?.name ?: result.ticketInfo?.attendeeName ?: "Unknown Attendee"
                binding.rollNo.text = "ID: ${ticketId.ifEmpty { "N/A" }}"
                
                binding.attendeeEmail.text = result.attendee?.email ?: ""
                binding.tierName.text = result.attendee?.tierName?.let { "Tier: $it" } ?: ""
            }
            
            var hasPaymentImage = false
            var hasExitImage = false

            val paymentImageStr = result.attendee?.paymentScreenshot
            if (!paymentImageStr.isNullOrEmpty() && paymentImageStr.startsWith("data:image")) {
                try {
                    val base64Image = paymentImageStr.substringAfter(",")
                    val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    binding.paymentImage.setImageBitmap(bitmap)
                    binding.paymentImage.visibility = View.VISIBLE
                    hasPaymentImage = true
                    binding.paymentImage.setOnClickListener { showFullScreenImage(bitmap) }
                } catch (e: Exception) {
                    binding.paymentImage.visibility = View.GONE
                }
            } else {
                binding.paymentImage.visibility = View.GONE
            }

            val exitImageStr = result.attendee?.exitImage
            if (!exitImageStr.isNullOrEmpty() && exitImageStr.startsWith("data:image")) {
                try {
                    val base64Image = exitImageStr.substringAfter(",")
                    val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    binding.exitImage.setImageBitmap(bitmap)
                    binding.exitImage.visibility = View.VISIBLE
                    hasExitImage = true
                    binding.exitImage.setOnClickListener { showFullScreenImage(bitmap) }
                } catch (e: Exception) {
                    binding.exitImage.visibility = View.GONE
                }
            } else {
                binding.exitImage.visibility = View.GONE
            }

            if (hasPaymentImage || hasExitImage) {
                binding.imagesContainer.visibility = View.VISIBLE
            } else {
                binding.imagesContainer.visibility = View.GONE
            }

            binding.btnTempExit.visibility = View.GONE
            binding.btnReEnter.visibility = View.GONE

            if (isScanMode) {
                binding.btnDismiss.text = "Scan Another"
                binding.resultIcon.visibility = View.VISIBLE
                if (result.success) {
                    binding.resultIcon.setImageResource(R.drawable.ic_success)
                    binding.resultTitle.text = "Access Granted"
                    binding.resultTitle.setTextColor(requireContext().getColor(R.color.success))
                } else if (result.status == "TEMPORARY_OUT") {
                    binding.resultIcon.setImageResource(R.drawable.ic_warning)
                    binding.resultTitle.text = "Re-entry Verification"
                    binding.resultTitle.setTextColor(requireContext().getColor(R.color.warning))
                    binding.btnReEnter.visibility = View.VISIBLE
                } else if (result.status == "INSIDE") {
                    binding.resultIcon.setImageResource(R.drawable.ic_error)
                    binding.resultTitle.text = "Already Inside"
                    binding.resultTitle.setTextColor(requireContext().getColor(R.color.error))
                    binding.btnTempExit.visibility = View.VISIBLE
                } else if (result.message.contains("pending", ignoreCase = true) || result.status == "PENDING") {
                    binding.resultIcon.setImageResource(R.drawable.ic_warning)
                    binding.resultTitle.text = "Pending Payment"
                    binding.resultTitle.setTextColor(requireContext().getColor(R.color.warning))
                } else {
                    binding.resultIcon.setImageResource(R.drawable.ic_error)
                    binding.resultTitle.text = "Invalid Ticket"
                    binding.resultTitle.setTextColor(requireContext().getColor(R.color.error))
                }
            } else {
                binding.btnDismiss.text = "Close"
                binding.resultIcon.visibility = View.GONE
                
                val statusText = when {
                    result.status == "TEMPORARY_OUT" -> "Temporarily Out"
                    result.status == "PENDING" -> "Pending Ticket"
                    result.status == "DECLINED" -> "Declined Ticket"
                    result.status == "INSIDE" -> "Inside Ticket"
                    else -> "Outside Ticket"
                }
                binding.resultTitle.text = statusText
                binding.resultTitle.setTextColor(requireContext().getColor(R.color.text_primary))
            }

            binding.btnTempExit.setOnClickListener {
                dismiss()
                onTempExitListener?.invoke(ticketId)
            }
            binding.btnReEnter.setOnClickListener {
                dismiss()
                onReEnterListener?.invoke(ticketId)
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
        
        fun newInstance(
            result: ScanResult, 
            isScanMode: Boolean = true, 
            onTempExit: ((String) -> Unit)? = null,
            onReEnter: ((String) -> Unit)? = null,
            onDismiss: () -> Unit
        ): ScanResultBottomSheet {
            return ScanResultBottomSheet().apply {
                this.scanResult = result
                this.isScanMode = isScanMode
                this.onTempExitListener = onTempExit
                this.onReEnterListener = onReEnter
                this.onDismissListener = onDismiss
            }
        }
    }
}
