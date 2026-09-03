package com.eventsphere.scanner.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.local.PreferencesManager
import com.eventsphere.scanner.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())

        // Add a small delay for splash effect
        Handler(Looper.getMainLooper()).postDelayed({
            checkNavigation()
        }, 2000)
    }

    private fun checkNavigation() {
        val baseUrl = preferencesManager.baseUrl
        val token = preferencesManager.accessToken

        // If baseUrl is default or not set, go to settings
        // Assuming DEFAULT_BASE_URL is "http://192.168.1.100:3000" and we want to ensure user configures it if needed
        // But the prompt says "If baseUrl is not set". 
        // I'll check if it's empty or the default placeholder that indicates it needs setup.
        // Actually, let's just check if it's specifically set by the user. 
        // For now, I'll assume if it's the default and the user wants to configure it.
        // Or I can just check if it's empty.
        
        if (baseUrl.isEmpty() || baseUrl == "http://192.168.1.100:3000") {
            // navigate to Settings
            findNavController().navigate(R.id.action_splashFragment_to_settingsFragment)
        } else if (token != null) {
            // navigate to Events
            findNavController().navigate(R.id.action_splashFragment_to_eventsFragment)
        } else {
            // navigate to Login
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
