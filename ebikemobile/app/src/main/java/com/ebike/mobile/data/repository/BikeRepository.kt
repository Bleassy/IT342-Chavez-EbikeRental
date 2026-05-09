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
            Timber.d("🔄 Fetching bikes (page=$page, size=$size)...")
            val response = api.getAllBikes(page, size)
            Timber.d("📡 API Response Code: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    Timber.d("📦 Response body keys: ${responseBody.keys}")
                    Timber.d("📦 Response body type: ${responseBody.javaClass.simpleName}")
                    
                    @Suppress("UNCHECKED_CAST")
                    val bikesList = when {
                        // Handle paginated response with "content" key (Spring Data Page response)
                        responseBody.containsKey("content") -> {
                            val contentList = responseBody["content"] as? List<*> ?: emptyList<Any>()
                            Timber.d("📋 Found 'content' key with ${contentList.size} items")
                            
                            // Convert each item (which is a Map) to Bike
                            val bikes = contentList.mapNotNull { item ->
                                if (item is Map<*, *>) {
                                    @Suppress("UNCHECKED_CAST")
                                    mapToBike(item as Map<String, Any>)
                                } else {
                                    Timber.w("Item is not a map: ${item?.javaClass?.simpleName}")
                                    null
                                }
                            }
                            Timber.d("✅ Mapped ${bikes.size} bikes from content")
                            bikes
                        }
                        // Handle direct array response with "data" key (Neon API response)
                        responseBody.containsKey("data") -> {
                            Timber.d("⚠️ No 'content' key found, checking 'data' key")
                            val dataList = responseBody["data"] as? List<*> ?: emptyList<Any>()
                            Timber.d("📋 Found 'data' key with ${dataList.size} items")
                            
                            val bikes = dataList.mapNotNull { item ->
                                if (item is Map<*, *>) {
                                    @Suppress("UNCHECKED_CAST")
                                    mapToBike(item as Map<String, Any>)
                                } else {
                                    Timber.w("Item is not a map: ${item?.javaClass?.simpleName}")
                                    null
                                }
                            }
                            Timber.d("✅ Mapped ${bikes.size} bikes from data")
                            bikes
                        }
                        // Fallback: treat entire response body as array of bikes
                        else -> {
                            Timber.d("⚠️ No 'content' or 'data' key found, treating as direct array")
                            val items = responseBody.values.filterIsInstance<Map<String, Any>>()
                            Timber.d("📋 Found ${items.size} map items")
                            items.mapNotNull { mapToBike(it) }
                        }
                    }
                    
                    Timber.d("✅ Successfully fetched ${bikesList.size} bikes")
                    if (bikesList.isEmpty()) {
                        Timber.w("⚠️ Bikes list is EMPTY!")
                    } else {
                        bikesList.forEach {
                            Timber.d("🚲 Bike: ${it.id} - ${it.name} - \$${it.hourlyRate}/hr - Battery: ${it.batteryLevel}%")
                        }
                    }
                    Result.success(bikesList)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch bikes"
                Timber.e("❌ API Error: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Get all bikes error")
            Result.failure(e)
        }
    }
    
    suspend fun getBikeDetail(bikeId: Long): Result<Bike> {
        return try {
            val response = api.getBikeDetail(bikeId)
            Timber.d("📡 Bike Detail API Response Code: ${response.code()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    val bikeData = apiResponse.data
                    if (bikeData != null) {
                        Timber.d("✅ Bike detail fetched: ${bikeData.bikeCode ?: bikeData.model}")
                        Result.success(bikeData)
                    } else {
                        Timber.e("❌ Bike data is null in response")
                        Result.failure(Exception("Bike data not found"))
                    }
                } else {
                    val message = apiResponse?.message ?: "Unknown error"
                    Timber.e("❌ API returned success=false: $message")
                    Result.failure(Exception(message))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch bike"
                Timber.e("❌ Bike detail error: $errorBody")
                Result.failure(Exception(errorBody))
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
                name = map["name"] as? String ?: map["bikeCode"] as? String,
                description = map["description"] as? String,
                model = map["model"] as? String,
                brand = map["brand"] as? String,
                bikeCode = map["bikeCode"] as? String,
                color = map["color"] as? String,
                year = (map["year"] as? Number)?.toInt(),
                type = map["type"] as? String,
                batteryLevel = (map["batteryLevel"] as? Number)?.toInt() ?: 0,
                gps = map["gps"] as? String,
                status = map["status"] as? String ?: "AVAILABLE",
                image = map["image"] as? String,
                imageUrl = map["imageUrl"] as? String,
                hourlyRate = ((map["hourlyRate"] as? Number)?.toDouble()) ?: ((map["pricePerHour"] as? Number)?.toDouble() ?: 0.0),
                pricePerHour = (map["pricePerHour"] as? Number)?.toDouble() ?: 0.0,
                dailyRate = ((map["dailyRate"] as? Number)?.toDouble()) ?: ((map["pricePerDay"] as? Number)?.toDouble() ?: 0.0),
                pricePerDay = (map["pricePerDay"] as? Number)?.toDouble() ?: 0.0,
                condition = map["condition"] as? String,
                location = map["location"] as? String,
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
