package com.ebike.mobile.data.repository

import android.content.Context
import com.ebike.mobile.api.BikeRentalApi
import com.ebike.mobile.api.RetrofitClient
import com.ebike.mobile.data.models.Booking
import com.ebike.mobile.data.models.BookingDTO
import timber.log.Timber

class BookingRepository(private val context: Context) {
    
    private val api by lazy { RetrofitClient.getClient(context).create(BikeRentalApi::class.java) }
    
    suspend fun createBooking(
        bikeId: Long,
        startTime: String,
        endTime: String
    ): Result<Booking> {
        return try {
            val booking = BookingDTO(
                id = 0,
                userId = 0,
                bikeId = bikeId,
                startTime = startTime,
                endTime = endTime,
                status = "PENDING"
            )
            val response = api.createBooking(booking)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Booking creation failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Create booking error")
            Result.failure(e)
        }
    }
    
    suspend fun getUserBookings(page: Int = 0, size: Int = 20): Result<List<Booking>> {
        return try {
            val response = api.getUserBookings(page, size)
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    @Suppress("UNCHECKED_CAST")
                    val bookings = (responseBody["content"] as? List<Map<String, Any>>)
                        ?.mapNotNull { mapToBooking(it) }
                        ?: emptyList()
                    Result.success(bookings)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch bookings"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get user bookings error")
            Result.failure(e)
        }
    }
    
    suspend fun getBookingDetail(bookingId: Long): Result<Booking> {
        return try {
            val response = api.getBookingDetail(bookingId)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch booking"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get booking detail error")
            Result.failure(e)
        }
    }
    
    suspend fun cancelBooking(bookingId: Long, reason: String = ""): Result<Booking> {
        return try {
            val request = mapOf("reason" to reason)
            val response = api.cancelBooking(bookingId, request)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Cancellation failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Cancel booking error")
            Result.failure(e)
        }
    }
    
    suspend fun completeBooking(bookingId: Long): Result<Booking> {
        return try {
            val response = api.completeBooking(bookingId)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Completion failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Complete booking error")
            Result.failure(e)
        }
    }
    
    suspend fun getAdminBookings(page: Int = 0, size: Int = 20): Result<List<Booking>> {
        return try {
            val response = api.getAllBookings(page, size)
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    @Suppress("UNCHECKED_CAST")
                    val bookings = (responseBody["content"] as? List<Map<String, Any>>)
                        ?.mapNotNull { mapToBooking(it) }
                        ?: emptyList()
                    Result.success(bookings)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch admin bookings"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get admin bookings error")
            Result.failure(e)
        }
    }
    
    private fun mapToBooking(map: Map<String, Any>): Booking? {
        return try {
            Booking(
                id = (map["id"] as? Number)?.toLong() ?: return null,
                userId = (map["userId"] as? Number)?.toLong() ?: 0,
                bikeId = (map["bikeId"] as? Number)?.toLong() ?: 0,
                startTime = map["startTime"] as? String ?: "",
                endTime = map["endTime"] as? String,
                status = map["status"] as? String ?: "PENDING",
                totalCost = (map["totalCost"] as? Number)?.toDouble(),
                cancellationReason = map["cancellationReason"] as? String,
                createdAt = map["createdAt"] as? String,
                updatedAt = map["updatedAt"] as? String
            )
        } catch (e: Exception) {
            Timber.e(e, "Error mapping booking")
            null
        }
    }
}
