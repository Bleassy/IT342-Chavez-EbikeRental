package com.ebike.mobile.data.repository

import android.content.Context
import com.ebike.mobile.api.BikeRentalApi
import com.ebike.mobile.api.RetrofitClient
import com.ebike.mobile.data.local.TokenManager
import com.ebike.mobile.data.models.*
import timber.log.Timber

class AuthRepository(private val context: Context) {
    
    private val api by lazy { RetrofitClient.getClient(context).create(BikeRentalApi::class.java) }
    private val tokenManager = TokenManager(context)
    
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = api.login(request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        // Save token and user data
                        tokenManager.saveToken(apiResponse.data.token)
                        tokenManager.saveUserData(
                            apiResponse.data.user.id,
                            apiResponse.data.user.email,
                            apiResponse.data.user.fullName,
                            apiResponse.data.user.role,
                            apiResponse.data.user.profilePic
                        )
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Login failed"))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("Login error: $errorBody")
                Result.failure(Exception(errorBody ?: "Login failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.failure(e)
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String? = null,
        address: String? = null
    ): Result<LoginResponse> {
        return try {
            val request = RegisterRequest(email, password, fullName, phone, address)
            val response = api.register(request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        // Save token and user data
                        tokenManager.saveToken(apiResponse.data.token)
                        tokenManager.saveUserData(
                            apiResponse.data.user.id,
                            apiResponse.data.user.email,
                            apiResponse.data.user.fullName,
                            apiResponse.data.user.role,
                            apiResponse.data.user.profilePic
                        )
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Registration failed"))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("Register error: $errorBody")
                Result.failure(Exception(errorBody ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Registration error")
            Result.failure(e)
        }
    }
    
    suspend fun loginWithGoogle(googleToken: String): Result<LoginResponse> {
        return try {
            val request = AuthGoogleRequest(googleToken)
            val response = api.loginWithGoogle(request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        // Save token and user data
                        tokenManager.saveToken(apiResponse.data.token)
                        tokenManager.saveGoogleToken(googleToken)
                        tokenManager.saveUserData(
                            apiResponse.data.user.id,
                            apiResponse.data.user.email,
                            apiResponse.data.user.fullName,
                            apiResponse.data.user.role,
                            apiResponse.data.user.profilePic
                        )
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Google login failed"))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("Google login error: $errorBody")
                Result.failure(Exception(errorBody ?: "Google login failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Google login error")
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        tokenManager.clearAll()
        RetrofitClient.resetClient()
    }
}
