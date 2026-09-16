package com.eventsphere.scanner.ui.tickets

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.api.models.AttendeeBasic
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.databinding.FragmentTicketListBinding
import com.eventsphere.scanner.ui.scanner.ScanResultBottomSheet
import kotlinx.coroutines.launch

class TicketListFragment : Fragment(R.layout.fragment_ticket_list) {

    private val args: TicketListFragmentArgs by navArgs()
    private val viewModel: TicketListViewModel by viewModels()
    
    private val ticketAdapter = TicketAdapter { ticket ->
        val scanResult = ScanResult(
            success = ticket.status.equals("INSIDE", ignoreCase = true),
            message = when (ticket.status.uppercase()) {
                "PENDING" -> "Ticket is pending verification."
                "DECLINED" -> "Ticket payment was declined."
                "INSIDE" -> "User is currently inside."
                "TEMPORARY_OUT" -> "User is temporarily out."
                else -> "User has not entered yet."
            },
            status = ticket.status.uppercase(),
            attendee = AttendeeBasic(
                id = ticket.id,
                name = ticket.attendeeName ?: "Unknown Attendee",
                email = ticket.attendeeEmail,
                rollNumber = null,
                tierName = ticket.tier,
                paymentScreenshot = ticket.paymentScreenshot
            ),
            ticketInfo = null
        )
        // Pass isScanMode = false to display as info rather than scan result
        ScanResultBottomSheet.newInstance(
            result = scanResult,
            isScanMode = false,
            onTempExit = null,
            onReEnter = null,
            onDismiss = {}
        ).show(parentFragmentManager, ScanResultBottomSheet.TAG)
    }
    
    private var _binding: FragmentTicketListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTicketListBinding.bind(view)

        setupToolbar()
        setupRecyclerView()

        viewModel.loadTickets(args.eventId, args.filterStatus)
        observeUiState()
    }

    private fun setupToolbar() {
        val title = when (args.filterStatus.uppercase()) {
            "ALL" -> "All Tickets"
            "INSIDE" -> "Inside Tickets"
            "OUTSIDE" -> "Outside Tickets"
            "PENDING" -> "Pending Tickets"
            "TEMPORARY_OUT" -> "Temporarily Out"
            else -> "Tickets"
        }
        binding.tvHeaderTitle.text = title
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = ticketAdapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is TicketListUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.recyclerView.isVisible = false
                            binding.tvEmpty.isVisible = false
                        }
                        is TicketListUiState.Success -> {
                            binding.progressBar.isVisible = false
                            if (state.tickets.isEmpty()) {
                                binding.recyclerView.isVisible = false
                                binding.tvEmpty.isVisible = true
                            } else {
                                binding.recyclerView.isVisible = true
                                binding.tvEmpty.isVisible = false
                                ticketAdapter.submitList(state.tickets)
                            }
                        }
                        is TicketListUiState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.recyclerView.isVisible = false
                            binding.tvEmpty.isVisible = true
                            binding.tvEmpty.text = state.message
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
