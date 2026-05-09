package com.ebike.mobile.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class User(
    val id: Long,
    val email: String,
    val fullName: String,
    val password: String? = null,
    val googleId: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val profilePic: String? = null,
    val nickname: String? = null,
    val role: String = "CUSTOMER",
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class Bike(
    val id: Long,
    val name: String? = null,
    val description: String? = null,
    val model: String? = null,
    val brand: String? = null,
    val bikeCode: String? = null,
    val color: String? = null,
    val year: Int? = null,
    val type: String? = null,
    val batteryLevel: Int = 0,
    val gps: String? = null,
    val status: String = "AVAILABLE", // AVAILABLE, RENTED, MAINTENANCE, OFFLINE
    val image: String? = null,
    val imageUrl: String? = null,
    val hourlyRate: Double = 0.0,
    val pricePerHour: Double = 0.0,
    val dailyRate: Double = 0.0,
    val pricePerDay: Double = 0.0,
    val condition: String? = null,
    val location: String? = null,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class Booking(
    val id: Long,
    val userId: Long,
    val bikeId: Long,
    val startTime: String,
    val endTime: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, ACTIVE, COMPLETED, CANCELLED
    val totalCost: Double? = null,
    val cancellationReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val bike: Bike? = null,
    val user: User? = null
) : Parcelable

@Parcelize
data class BookingDTO(
    val id: Long,
    val userId: Long,
    val bikeId: Long,
    val startTime: String,
    val endTime: String? = null,
    val status: String,
    val totalCost: Double? = null,
    val cancellationReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class LoginRequest(
    val email: String,
    val password: String
) : Parcelable

// Actual response format from backend
@Parcelize
data class AuthResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val token: String,
    val role: String
) : Parcelable

// Wrapper for backend response
@Parcelize
data class LoginResponse(
    val token: String,
    val user: User
) : Parcelable

@Parcelize
data class AuthApiResponse(
    val success: Boolean,
    val message: String,
    val data: AuthResponse? = null,
    val errors: Map<String, String>? = null
) : Parcelable

@Parcelize
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val address: String? = null
) : Parcelable

@Parcelize
data class AuthGoogleRequest(
    val idToken: String? = null,
    val code: String? = null,
    val redirectUri: String? = null
) : Parcelable

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data:  T? = null,
    val error: String? = null
)
