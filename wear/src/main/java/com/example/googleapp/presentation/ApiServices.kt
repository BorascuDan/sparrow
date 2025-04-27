//ApiServices.kt
package com.example.googleapp.api

import android.location.LocationRequest
import com.example.googleapp.models.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @GET("api/users/get")
    fun getUserData(@Header("Authorization") token: String): Call<ApiResponse<String>>

    @GET("api/senzor/lastTemp")
    fun getLastTemp(@Header("Authorization") token: String): Call<TemperatureResponse>

    @GET("api/drink/pahar")
    fun getTotalDrinks(@Header("Authorization") token: String): Call<ApiResponse<Int>>

    @POST("your_endpoint_here")
    suspend fun sendSignal(
        @Header("Authorization") authToken: String,
        @Body requestBody: RequestBody
    ): Call<ApiResponse<Int>>
}