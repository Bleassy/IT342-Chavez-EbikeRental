package com.chavez.mobile2.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Data classes for requests and responses

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?
)
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse?
)

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
