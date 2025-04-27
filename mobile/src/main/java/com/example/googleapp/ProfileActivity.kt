package com.example.googleapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.googleapp.api.RetrofitClient
import com.example.googleapp.models.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textViewUsername: TextView
    private lateinit var textViewAge: TextView
    private lateinit var textViewWeight: TextView
    private lateinit var textViewHeight: TextView
    private lateinit var textViewGender: TextView
    private lateinit var textViewProfileStatus: TextView
    private lateinit var buttonDeleteAccount: Button
    private lateinit var buttonBack: Button
    private val token = "Bearer 8" // Using the specified token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_main)) { v, insets ->
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
        textViewUsername = findViewById(R.id.textViewUsername)
        textViewAge = findViewById(R.id.textViewAge)
        textViewWeight = findViewById(R.id.textViewWeight)
        textViewHeight = findViewById(R.id.textViewHeight)
        textViewGender = findViewById(R.id.textViewGender)
        textViewProfileStatus = findViewById(R.id.textViewProfileStatus)
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount)
        buttonBack = findViewById(R.id.buttonBack)

        // Set up button click listeners
        buttonDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        buttonBack.setOnClickListener {
            finish() // Go back to previous activity
        }

        // Set hardcoded user data
        displayHardcodedUserData()
    }

    private fun displayHardcodedUserData() {
        // Set hardcoded username
        textViewUsername.text = "Username: JohnDoe"

        // Set hardcoded profile data using the values from the original code
        textViewAge.text = "Age: 24"
        textViewWeight.text = "Weight: 84 kg"
        textViewHeight.text = "Height: 192 cm"
        textViewGender.text = "Gender: Male"
        textViewProfileStatus.text = "Profile Status: Active"
    }

    private fun deleteAccount() {
        val call = RetrofitClient.instance.deleteUser(token)
        call.enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        // Clear SharedPreferences
                        sharedPreferences.edit().clear().apply()

                        // Let the Wear OS know about the logout
                        WearDataLayerService.sendAuthStatusToWear(this@ProfileActivity)

                        Toast.makeText(
                            this@ProfileActivity,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to login screen
                        startActivity(Intent(this@ProfileActivity, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finishAffinity() // Close all activities
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Error deleting account: ${apiResponse?.message ?: "Unknown error"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Error: HTTP ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}