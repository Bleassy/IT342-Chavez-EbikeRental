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
                response.body()?.let {
                    // Save token and user data
                    tokenManager.saveToken(it.token)
                    tokenManager.saveUserData(
                        it.user.id,
                        it.user.email,
                        it.user.fullName,
                        it.user.role,
                        it.user.profilePic
                    )
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
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
                response.body()?.let {
                    // Save token and user data
                    tokenManager.saveToken(it.token)
                    tokenManager.saveUserData(
                        it.user.id,
                        it.user.email,
                        it.user.fullName,
                        it.user.role,
                        it.user.profilePic
                    )
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
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
                response.body()?.let {
                    // Save token and user data
                    tokenManager.saveToken(it.token)
                    tokenManager.saveGoogleToken(googleToken)
                    tokenManager.saveUserData(
                        it.user.id,
                        it.user.email,
                        it.user.fullName,
                        it.user.role,
                        it.user.profilePic
                    )
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Google login failed"))
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
