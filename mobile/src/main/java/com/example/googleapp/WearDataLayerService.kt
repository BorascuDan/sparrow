package com.example.googleapp

import android.content.SharedPreferences
import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WearDataLayerService {
    private const val AUTH_PATH = "/auth_status"
    private const val AUTH_KEY = "auth_token"
    private const val USERNAME_KEY = "username"

    fun sendAuthStatusToWear(context: Context) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("AUTH_TOKEN", "") ?: ""
        val username = sharedPreferences.getString("USERNAME", "") ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataClient = Wearable.getDataClient(context)
                val putDataMapReq = PutDataMapRequest.create(AUTH_PATH)

                putDataMapReq.dataMap.putString(AUTH_KEY, token)
                putDataMapReq.dataMap.putString(USERNAME_KEY, username)
                putDataMapReq.setUrgent()

                val putDataReq = putDataMapReq.asPutDataRequest()
                Tasks.await(dataClient.putDataItem(putDataReq))
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
}