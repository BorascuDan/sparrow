//ApiServices.kt
package com.example.googleapp.api

import com.example.googleapp.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @GET("api/senzor/lastTemp")
    fun getLastTemp(@Header("Authorization") token: String): Call<TemperatureResponse>
}