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

        if (baseUrl.isEmpty()) {
            // navigate to Settings
            findNavController().navigate(R.id.action_splashFragment_to_settingsFragment)
        } else if (token != null) {
            // navigate to Login to show Biometric or use token
            // we will let login fragment handle biometric auto-login
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
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
