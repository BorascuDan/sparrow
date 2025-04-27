//ApiModels.kt
package com.example.googleapp.models


data class TemperatureResponse(
    val success: Boolean,
    val status: Int,
    val message: String,
    val data: Float
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T?
)