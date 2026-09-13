package com.eventsphere.scanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eventsphere.scanner.data.api.RetrofitClient
import com.eventsphere.scanner.data.local.PreferencesManager
import com.eventsphere.scanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize API FIRST before Android restores the fragments!
        RetrofitClient.getApi(
            PreferencesManager(this)
        )
        
        super.onCreate(savedInstanceState) // Move this below the API init!
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
