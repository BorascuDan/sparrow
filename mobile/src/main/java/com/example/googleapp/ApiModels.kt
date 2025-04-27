package com.example.googleapp.models

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserProfileData(
    val user_id: String,
    val age: Int?,
    val weight: Float?,
    val height: Int?,
    val gender: Boolean,
    val profile_completed: Boolean
)

data class LocationRequest(
    val location: String
)

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

// Generic ApiResponse that can handle different data types
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)