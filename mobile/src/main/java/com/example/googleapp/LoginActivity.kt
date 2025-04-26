package com.example.googleapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.googleapp.api.RetrofitClient
import com.example.googleapp.models.ApiResponse
import com.example.googleapp.models.LoginRequest
import com.example.googleapp.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLinkTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        registerLinkTextView = findViewById(R.id.textViewRegisterLink)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.contains("USER_ID")) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Login button click listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        // Register link click listener
        registerLinkTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }

        return true
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.instance.loginUser(loginRequest)
            .enqueue(object : Callback<ApiResponse<Map<String, User>>> {
                override fun onResponse(
                    call: Call<ApiResponse<Map<String, User>>>,
                    response: Response<ApiResponse<Map<String, User>>>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            val user = apiResponse.data?.get("user")
                            if (user != null) {
                                // Save user data in SharedPreferences
                                val editor = sharedPreferences.edit()
                                editor.putInt("USER_ID", user.id)
                                editor.putString("USERNAME", user.username)
                                editor.putString("EMAIL", user.email)
                                editor.apply()

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate to main screen
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                apiResponse?.message ?: "Login failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed. Please check your credentials.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Map<String, User>>>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}