package com.example.googleapp.service

import android.content.Context
import android.util.Log
import com.example.googleapp.api.RetrofitClient
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import kotlin.random.Random

/**
 * Service that sends a signal '1' to a server at random intervals between 20-30 seconds.
 * The interval changes after each send.
 */
class ServerSignalSender(private val context: Context) {
    private val TAG = "ServerSignalSender"
    private var job: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Starts the service to send signals at random intervals
     */
    fun startSendingSignals(authToken: String) {
        if (job != null) return  // Already running

        job = coroutineScope.launch {
            Log.d(TAG, "Starting server signal sender")

            while (isActive) {
                try {
                    // Send the signal '1'
                    sendSignal(authToken)

                    // Generate new random interval between 20-30 seconds
                    val intervalSeconds = Random.nextInt(20, 31)
                    Log.d(TAG, "Next signal in $intervalSeconds seconds")

                    // Wait for the interval
                    delay(intervalSeconds * 1000L)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending signal: ${e.message}")
                    delay(5000) // Wait 5 seconds before retry on error
                }
            }
        }
    }

    /**
     * Stops the signal sending service
     */
    fun stopSendingSignals() {
        job?.cancel()
        job = null
        Log.d(TAG, "Server signal sender stopped")
    }

    /**
     * Sends the signal to the server
     */
    private suspend fun sendSignal(authToken: String) {
        try {
            val signal = "1"
            val requestBody = signal.toRequestBody("text/plain".toMediaType())

            // Using your existing RetrofitClient structure
            val response = RetrofitClient.instance.sendSignal("Bearer $authToken", requestBody)

            if (response.isSuccessful) {
                Log.d(TAG, "Signal sent successfully")
            } else {
                Log.e(TAG, "Failed to send signal: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending signal: ${e.message}")
            throw e  // Rethrow to be handled by the caller
        }
    }

    /**
     * Check if the sender is currently active
     */
    fun isActive(): Boolean {
        return job?.isActive == true
    }
}