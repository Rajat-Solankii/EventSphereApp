package com.eventsphere.scanner.ui.events

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.eventsphere.scanner.R
import com.eventsphere.scanner.databinding.FragmentEventsBinding
import kotlinx.coroutines.launch

class EventsFragment : Fragment(R.layout.fragment_events) {

    private val viewModel: EventsViewModel by viewModels()
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: EventAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventsBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        observeUiState()

        binding.btnRetry.setOnClickListener {
            viewModel.fetchEvents()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(R.id.action_eventsFragment_to_profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter { event ->
            val action = EventsFragmentDirections.actionEventsFragmentToDashboardFragment(event.id)
            findNavController().navigate(action)
        }
        binding.rvEvents.adapter = adapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EventsUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.rvEvents.isVisible = false
                            binding.layoutEmpty.isVisible = false
                            binding.layoutError.isVisible = false
                        }
                        is EventsUiState.Success -> {
                            binding.progressBar.isVisible = false
                            binding.rvEvents.isVisible = true
                            binding.layoutEmpty.isVisible = false
                            binding.layoutError.isVisible = false
                            adapter.submitList(state.events)
                        }
                        is EventsUiState.Empty -> {
                            binding.progressBar.isVisible = false
                            binding.rvEvents.isVisible = false
                            binding.layoutEmpty.isVisible = true
                            binding.layoutError.isVisible = false
                        }
                        is EventsUiState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.rvEvents.isVisible = false
                            binding.layoutEmpty.isVisible = false
                            binding.layoutError.isVisible = true
                            binding.tvErrorMessage.text = state.message
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
