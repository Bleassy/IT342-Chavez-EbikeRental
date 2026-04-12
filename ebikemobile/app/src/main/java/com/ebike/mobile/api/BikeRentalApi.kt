package com.ebike.mobile.api

import com.ebike.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface BikeRentalApi {
    
    // Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthApiResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthApiResponse>
    
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: AuthGoogleRequest): Response<AuthApiResponse>
    
    // Users
    @GET("users/profile")
    suspend fun getUserProfile(): Response<User>
    
    @PUT("users/profile")
    suspend fun updateProfile(@Body user: User): Response<User>
    
    @POST("users/profile-pic")
    suspend fun uploadProfilePic(@Body request: Map<String, String>): Response<User>
    
    // Bikes
    @GET("bikes")
    suspend fun getAllBikes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<Map<String, Any>>
    
    @GET("bikes/{id}")
    suspend fun getBikeDetail(@Path("id") bikeId: Long): Response<Bike>
    
    @GET("bikes/search")
    suspend fun searchBikes(@Query("query") query: String): Response<List<Bike>>
    
    @GET("bikes/nearby")
    suspend fun getNearbyBikes(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Double = 5.0
    ): Response<List<Bike>>
    
    // Bookings
    @POST("bookings")
    suspend fun createBooking(@Body booking: BookingDTO): Response<Booking>
    
    @GET("bookings")
    suspend fun getUserBookings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<Map<String, Any>>
    
    @GET("bookings/{id}")
    suspend fun getBookingDetail(@Path("id") bookingId: Long): Response<Booking>
    
    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: Long,
        @Body request: Map<String, String>
    ): Response<Booking>
    
    @PUT("bookings/{id}/complete")
    suspend fun completeBooking(@Path("id") bookingId: Long): Response<Booking>
    
    // Admin endpoints
    @GET("admin/bookings")
    suspend fun getAllBookings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<Map<String, Any>>
    
    @POST("admin/bikes")
    suspend fun createBike(@Body bike: Bike): Response<Bike>
    
    @PUT("admin/bikes/{id}")
    suspend fun updateBike(@Path("id") bikeId: Long, @Body bike: Bike): Response<Bike>
    
    @DELETE("admin/bikes/{id}")
    suspend fun deleteBike(@Path("id") bikeId: Long): Response<Map<String, String>>
}
