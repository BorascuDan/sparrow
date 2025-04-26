package com.example.googleapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button

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

        // Display welcome message
        val username = sharedPreferences.getString("USERNAME", "User")
        welcomeTextView.text = "Welcome, $username!"

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
}