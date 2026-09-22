package com.eventsphere.scanner.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.local.PreferencesManager
import com.eventsphere.scanner.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var shouldAutoPromptBiometric = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())

        setupListeners()
        observeUiState()
        setupBiometric()
    }

    override fun onResume() {
        super.onResume()
        if (shouldAutoPromptBiometric) {
            shouldAutoPromptBiometric = false
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Proceed to Events if authentication succeeds
                    findNavController().navigate(R.id.action_loginFragment_to_eventsFragment)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for EventSphere")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val token = preferencesManager.accessToken
        if (!token.isNullOrEmpty()) {
            binding.btnBiometric.visibility = View.VISIBLE
            binding.btnBiometric.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
            // Queue auto-prompt when fragment fully resumes
            shouldAutoPromptBiometric = true
        } else {
            binding.btnBiometric.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.etEmail.doAfterTextChanged { viewModel.resetError() }
        binding.etPassword.doAfterTextChanged { viewModel.resetError() }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_settingsFragment)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: LoginUiState) {
        when (state) {
            is LoginUiState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvError.visibility = View.GONE
            }
            is LoginUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                binding.tvError.visibility = View.GONE
            }
            is LoginUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                findNavController().navigate(R.id.action_loginFragment_to_eventsFragment)
            }
            is LoginUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.tvError.text = state.message
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
