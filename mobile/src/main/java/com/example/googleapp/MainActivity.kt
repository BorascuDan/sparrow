package com.example.googleapp

import android.net.Uri
import android.widget.Toast
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.googleapp.api.RetrofitClient
import com.example.googleapp.models.ApiResponse
import com.example.googleapp.models.LocationRequest
import com.example.googleapp.models.TemperatureResponse
import com.example.googleapp.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewWelcome: TextView
    private lateinit var textViewTotalDrinksToday: TextView
    private lateinit var textViewTotalDrinksAtLocation: TextView
    private lateinit var logoutButton: Button
    private lateinit var imageViewDrunkness: ImageView
    private lateinit var buttonOpenMaps: Button
    private lateinit var buttonOrderUber: Button
    private lateinit var buttonProfile: Button  // Added profile button
    private val handler = Handler(Looper.getMainLooper())

    // Updated runnable that will refresh all data every 4 seconds
    private val updateDataRunnable = object : Runnable {
        override fun run() {
            val token = "Bearer 8" // Using the specified token

            // Fetch all data
            fetchUserData()
            fetchTemperature(token)
            fetchTotalDrinks(token)
            fetchLocationDrinks(token)
            fetchDrunknessLevel(token)

            // Schedule the next update in 4 seconds
            handler.postDelayed(this, 4000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        imageViewDrunkness = findViewById(R.id.imageViewDrunkness)
        textViewWelcome = findViewById(R.id.textViewWelcome)
        textViewTotalDrinksToday = findViewById(R.id.textViewTotalDrinksToday)
        textViewTotalDrinksAtLocation = findViewById(R.id.textViewTotalDrinksAtLocation)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        logoutButton = findViewById(R.id.buttonLogout)
        buttonOpenMaps = findViewById(R.id.buttonOpenMaps)
        buttonOrderUber = findViewById(R.id.buttonOrderUber)
        buttonProfile = findViewById(R.id.buttonProfile)  // Initialize the profile button

        // Set click listener for the Profile button
        buttonProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Set click listener for the Open Maps button
        buttonOpenMaps.setOnClickListener {
            openGoogleMaps()
        }

        // Set click listener for the Order Uber button
        buttonOrderUber.setOnClickListener {
            orderUber()
        }

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

        // Logout button click listener
        logoutButton.setOnClickListener {
            // Clear SharedPreferences
            sharedPreferences.edit().clear().apply()

            WearDataLayerService.sendAuthStatusToWear(this)

            // Navigate to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Initial data fetch (will also be refreshed by the runnable)
        handler.post(updateDataRunnable)
    }

    override fun onStart() {
        super.onStart()
        // Start periodic data updates if not already running
        if (!handler.hasCallbacks(updateDataRunnable)) {
            handler.post(updateDataRunnable)
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop periodic updates when activity is not visible
        handler.removeCallbacks(updateDataRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up handler to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }

    private fun fetchUserData() {
        val token = "Bearer 8" // Using the specified token
        val call = RetrofitClient.instance.getUserData(token)
        call.enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(call: Call<ApiResponse<String>>, response: Response<ApiResponse<String>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val username = apiResponse.data
                        textViewWelcome.text = "Welcome back, $username!"
                    } else {
                        textViewWelcome.text = "Welcome back!"
                    }
                } else {
                    textViewWelcome.text = "Welcome back!"
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                textViewWelcome.text = "Welcome back!"
            }
        })
    }

    private fun fetchTotalDrinks(token: String) {
        val call = RetrofitClient.instance.getTotalDrinks(token)
        call.enqueue(object : Callback<ApiResponse<Int>> {
            override fun onResponse(call: Call<ApiResponse<Int>>, response: Response<ApiResponse<Int>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val totalDrinks = apiResponse.data
                        textViewTotalDrinksToday.text = "Total Drinks Today: $totalDrinks"
                    } else {
                        textViewTotalDrinksToday.text = "Total Drinks Today: 0"
                    }
                } else {
                    textViewTotalDrinksToday.text = "Total Drinks Today: 0"
                }
            }

            override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
                textViewTotalDrinksToday.text = "Total Drinks Today: 0"
            }
        })
    }

    private fun fetchLocationDrinks(token: String) {
        val locationRequest = LocationRequest(location = "1") // Set location to 1 as required
        val call = RetrofitClient.instance.getLocationDrinks(token, locationRequest)
        call.enqueue(object : Callback<ApiResponse<Int>> {
            override fun onResponse(call: Call<ApiResponse<Int>>, response: Response<ApiResponse<Int>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val locationDrinks = apiResponse.data
                        textViewTotalDrinksAtLocation.text = "Total Drinks at Location: $locationDrinks"
                    } else {
                        textViewTotalDrinksAtLocation.text = "Total Drinks at Location: 0"
                    }
                } else {
                    textViewTotalDrinksAtLocation.text = "Total Drinks at Location: 0"
                }
            }

            override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
                textViewTotalDrinksAtLocation.text = "Total Drinks at Location: 0"
            }
        })
    }

    private fun fetchDrunknessLevel(token: String) {
        val call = RetrofitClient.instance.getTotalDrinks(token)
        call.enqueue(object : Callback<ApiResponse<Int>> {
            override fun onResponse(call: Call<ApiResponse<Int>>, response: Response<ApiResponse<Int>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val totalDrinks = apiResponse.data
                        // Calculate drunkness level based on drinks
                        // We'll map the number of drinks to a level between 1-5
                        val level = when {
                            totalDrinks >= 20 -> 5
                            totalDrinks >= 15 -> 4
                            totalDrinks >= 10 -> 3
                            totalDrinks >= 5 -> 2
                            else -> 1
                        }
                        updateDrunknessLevel(level)
                    } else {
                        updateDrunknessLevel(1)
                    }
                } else {
                    updateDrunknessLevel(1)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Int>>, t: Throwable) {
                updateDrunknessLevel(1)
            }
        })
    }

    private fun updateDrunknessLevel(level: Int) {
        val drawableResource = when (level.coerceIn(1, 5)) {
            5 -> R.drawable.beer_pint_1
            4 -> R.drawable.beer_pint_2
            3 -> R.drawable.beer_pint_3
            2 -> R.drawable.beer_pint_4
            1 -> R.drawable.beer_pint_5
            else -> R.drawable.beer_pint_1 // Fallback to level 1
        }

        imageViewDrunkness.setImageResource(drawableResource)
    }

    private fun fetchTemperature(token: String) {
        val call = RetrofitClient.instance.getLastTemp(token)
        call.enqueue(object : Callback<TemperatureResponse> {
            override fun onResponse(call: Call<TemperatureResponse>, response: Response<TemperatureResponse>) {
                if (response.isSuccessful) {
                    val tempResponse = response.body()
                    if (tempResponse?.success == true) {
                        textViewTemperature.text = "${tempResponse.data}°C"
                    } else {
                        textViewTemperature.text = "Error: ${tempResponse?.message ?: "Unknown"}"
                    }
                } else {
                    textViewTemperature.text = "Error: HTTP ${response.code()}"
                }
            }

            override fun onFailure(call: Call<TemperatureResponse>, t: Throwable) {
                textViewTemperature.text = "Error: ${t.message}"
            }
        })
    }

    /**
     * Opens the Uber app or directs to Google Play Store to download it
     */
    private fun orderUber() {
        try {
            // Try to open Uber app directly
            val uberPackageName = "com.ubercab"
            val uberIntent = packageManager.getLaunchIntentForPackage(uberPackageName)

            if (uberIntent != null) {
                // Uber is installed, open it
                startActivity(uberIntent)
            } else {
                // If Uber isn't installed, take user to Play Store
                try {
                    // Open Google Play Store app to Uber's page
                    val playStoreIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$uberPackageName"))
                    startActivity(playStoreIntent)
                } catch (e: Exception) {
                    // If Play Store app isn't available, open web browser to Play Store
                    val webPlayStoreIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$uberPackageName"))
                    startActivity(webPlayStoreIntent)

                    Toast.makeText(this, "Opening Play Store in browser",
                        Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open Uber or Play Store: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGoogleMaps() {
        try {
            // Show bars near current location
            val gmmIntentUri = Uri.parse("geo:0,0?q=bars+nearby")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps") // Specify Google Maps package

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // If Google Maps isn't installed, open in browser
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=bars+nearby"))
                startActivity(browserIntent)

                // Optional: Show a toast informing the user
                Toast.makeText(this, "Google Maps app not found. Opening in browser.",
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Handle exceptions
            Toast.makeText(this, "Could not open maps: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }
}