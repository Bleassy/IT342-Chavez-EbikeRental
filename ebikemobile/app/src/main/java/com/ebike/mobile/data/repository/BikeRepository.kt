package com.ebike.mobile.data.repository

import android.content.Context
import com.ebike.mobile.api.BikeRentalApi
import com.ebike.mobile.api.RetrofitClient
import com.ebike.mobile.data.models.Bike
import timber.log.Timber

class BikeRepository(private val context: Context) {
    
    private val api by lazy { RetrofitClient.getClient(context).create(BikeRentalApi::class.java) }
    
    suspend fun getAllBikes(page: Int = 0, size: Int = 20): Result<List<Bike>> {
        return try {
            val response = api.getAllBikes(page, size)
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    @Suppress("UNCHECKED_CAST")
                    val bikes = (responseBody["content"] as? List<Map<String, Any>>)
                        ?.mapNotNull { mapToBike(it) }
                        ?: emptyList()
                    Result.success(bikes)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch bikes"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get all bikes error")
            Result.failure(e)
        }
    }
    
    suspend fun getBikeDetail(bikeId: Long): Result<Bike> {
        return try {
            val response = api.getBikeDetail(bikeId)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch bike"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get bike detail error")
            Result.failure(e)
        }
    }
    
    suspend fun searchBikes(query: String): Result<List<Bike>> {
        return try {
            val response = api.searchBikes(query)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Search failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Search bikes error")
            Result.failure(e)
        }
    }
    
    suspend fun getNearbyBikes(
        latitude: Double,
        longitude: Double,
        radius: Double = 5.0
    ): Result<List<Bike>> {
        return try {
            val response = api.getNearbyBikes(latitude, longitude, radius)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch nearby bikes"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Get nearby bikes error")
            Result.failure(e)
        }
    }
    
    private fun mapToBike(map: Map<String, Any>): Bike? {
        return try {
            Bike(
                id = (map["id"] as? Number)?.toLong() ?: return null,
                name = map["name"] as? String ?: "",
                description = map["description"] as? String,
                model = map["model"] as? String ?: "",
                color = map["color"] as? String ?: "",
                batteryLevel = (map["batteryLevel"] as? Number)?.toInt() ?: 0,
                gps = map["gps"] as? String,
                status = map["status"] as? String ?: "AVAILABLE",
                image = map["image"] as? String,
                hourlyRate = (map["hourlyRate"] as? Number)?.toDouble() ?: 0.0,
                dailyRate = (map["dailyRate"] as? Number)?.toDouble() ?: 0.0,
                locationLat = (map["locationLat"] as? Number)?.toDouble(),
                locationLng = (map["locationLng"] as? Number)?.toDouble(),
                createdAt = map["createdAt"] as? String,
                updatedAt = map["updatedAt"] as? String
            )
        } catch (e: Exception) {
            Timber.e(e, "Error mapping bike")
            null
        }
    }
}
