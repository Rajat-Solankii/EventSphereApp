package com.eventsphere.scanner.ui.dashboard

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
import com.eventsphere.scanner.R
import com.eventsphere.scanner.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val args: DashboardFragmentArgs by navArgs()
    private val viewModel: DashboardViewModel by viewModels()
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        viewModel.loadData(args.eventId)

        observeUiState()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnStartScanning.setOnClickListener {
            val action = DashboardFragmentDirections.actionDashboardFragmentToScannerFragment(args.eventId)
            findNavController().navigate(action)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadData(args.eventId)
        }

        setupCardClickListeners()
    }

    private fun setupCardClickListeners() {
        binding.cardTotal.setOnClickListener { navigateToTicketList("ALL") }
        binding.cardInside.setOnClickListener { navigateToTicketList("INSIDE") }
        binding.cardOutside.setOnClickListener { navigateToTicketList("OUTSIDE") }
        binding.cardPending.setOnClickListener { navigateToTicketList("PENDING") }
    }

    private fun navigateToTicketList(filterStatus: String) {
        val action = DashboardFragmentDirections.actionDashboardFragmentToTicketListFragment(args.eventId, filterStatus)
        findNavController().navigate(action)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DashboardUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.scrollView.isVisible = false
                            binding.errorLayout.isVisible = false
                            binding.btnStartScanning.isEnabled = false
                        }
                        is DashboardUiState.Success -> {
                            binding.progressBar.isVisible = false
                            binding.scrollView.isVisible = true
                            binding.errorLayout.isVisible = false
                            binding.btnStartScanning.isEnabled = true
                            updateUi(state)
                        }
                        is DashboardUiState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.scrollView.isVisible = false
                            binding.errorLayout.isVisible = true
                            binding.btnStartScanning.isEnabled = false
                            binding.tvError.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun updateUi(state: DashboardUiState.Success) {
        binding.tvEventTitle.text = state.event.title
        binding.tvEventVenue.text = state.event.venue
        
        binding.tvStatTotal.text = state.stats.total.toString()
        binding.tvStatInside.text = state.stats.inside.toString()
        binding.tvStatOutside.text = state.stats.outside.toString()
        binding.tvStatPending.text = state.stats.pending.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
