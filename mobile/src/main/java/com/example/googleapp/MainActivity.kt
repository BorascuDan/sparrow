package com.example.googleapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.googleapp.api.RetrofitClient
import com.example.googleapp.models.TemperatureResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private val updateTemperatureRunnable = object : Runnable {
        override fun run() {
            val token = sharedPreferences.getString("AUTH_TOKEN", "") ?: ""
            if (token.isNotEmpty()) {
                fetchTemperature(token)
            } else {
                welcomeTextView.text = "Error: No token found"
            }
            // Schedule the next update in 4 seconds
            handler.postDelayed(this, 4000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Check if user is logged in
        if (!sharedPreferences.contains("USER_ID")) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize views
        welcomeTextView = findViewById(R.id.textViewWelcome)
        logoutButton = findViewById(R.id.buttonLogout)

        // Logout button click listener
        logoutButton.setOnClickListener {
            // Clear SharedPreferences
            sharedPreferences.edit().clear().apply()

            WearDataLayerService.sendAuthStatusToWear(this)

            // Navigate to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Start periodic temperature updates
        handler.post(updateTemperatureRunnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop periodic updates when activity is not visible
        handler.removeCallbacks(updateTemperatureRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up handler to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }

    private fun fetchTemperature(token: String) {
        val call = RetrofitClient.instance.getLastTemp("Bearer $token")
        call.enqueue(object : Callback<TemperatureResponse> {
            override fun onResponse(call: Call<TemperatureResponse>, response: Response<TemperatureResponse>) {
                if (response.isSuccessful) {
                    val tempResponse = response.body()
                    if (tempResponse?.success == true) {
                        welcomeTextView.text = "Temperature: ${tempResponse.data}°C"
                    } else {
                        welcomeTextView.text = "Error: ${tempResponse?.message ?: "Unknown error"}"
                    }
                } else {
                    welcomeTextView.text = "Error: Failed to fetch temperature (HTTP ${response.code()})"
                }
            }

            override fun onFailure(call: Call<TemperatureResponse>, t: Throwable) {
                welcomeTextView.text = "Error: ${t.message}"
            }
        })
    }
}