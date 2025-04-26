package com.example.googleapp.api

import com.example.googleapp.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/register")
    fun registerUser(@Body registerRequest: RegisterRequest): Call<ApiResponse<User>>

    @POST("api/users/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<ApiResponse<Map<String, User>>>
}