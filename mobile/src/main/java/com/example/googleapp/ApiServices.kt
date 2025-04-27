package com.example.googleapp.api

import com.example.googleapp.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/register")
    fun registerUser(@Body registerRequest: RegisterRequest): Call<ApiResponse<User>>

    @POST("api/users/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<ApiResponse<Map<String, User>>>

    @GET("api/users/get")
    fun getUserData(@Header("Authorization") token: String): Call<ApiResponse<String>>

    @GET("api/senzor/lastTemp")
    fun getLastTemp(@Header("Authorization") token: String): Call<TemperatureResponse>

    @GET("api/drink/pahar")
    fun getTotalDrinks(@Header("Authorization") token: String): Call<ApiResponse<Int>>

    @POST("api/drink/locatie")
    fun getLocationDrinks(
        @Header("Authorization") token: String,
        @Body locationRequest: LocationRequest
    ): Call<ApiResponse<Int>>
}