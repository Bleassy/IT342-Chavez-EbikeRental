package com.ebike.mobile.data.repository

import android.content.Context
import com.ebike.mobile.api.BikeRentalApi
import com.ebike.mobile.api.RetrofitClient
import com.ebike.mobile.data.local.TokenManager
import com.ebike.mobile.data.models.*
import kotlinx.coroutines.flow.first
import timber.log.Timber

class AuthRepository(private val context: Context) {
    
    private val api by lazy { RetrofitClient.getClient(context).create(BikeRentalApi::class.java) }
    private val tokenManager = TokenManager(context)

    private fun normalizeToken(rawToken: String?): String? {
        val cleaned = rawToken
            ?.trim()
            ?.removePrefix("Bearer ")
            ?.removePrefix("bearer ")
            ?.removeSurrounding("\"")
            ?.trim()
        return cleaned?.takeIf { it.isNotEmpty() }
    }

    private fun splitFullName(fullName: String): Pair<String, String> {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "" to ""
            parts.size == 1 -> parts.first() to ""
            else -> parts.first() to parts.drop(1).joinToString(" ")
        }
    }

    private fun mapBackendUser(profile: BackendUserProfileDto): User {
        val fullName = listOfNotNull(profile.firstName, profile.lastName)
            .joinToString(" ")
            .trim()

        return User(
            id = profile.id ?: 0L,
            email = profile.email.orEmpty(),
            fullName = fullName,
            phone = profile.phone,
            address = profile.address,
            profilePic = profile.profilePictureUrl,
            nickname = profile.nickname,
            role = profile.role ?: "CUSTOMER",
            createdAt = profile.createdAt,
            updatedAt = profile.updatedAt
        )
    }

    private suspend fun getAuthorizationHeader(): String {
        val token = normalizeToken(tokenManager.getAccessToken().first())
        return if (!token.isNullOrBlank()) {
            "Bearer $token"
        } else {
            throw IllegalStateException("No access token available. Please log in again.")
        }
    }
    
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = api.login(request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        // Convert AuthResponse to LoginResponse
                        val user = User(
                            id = authData.id,
                            email = authData.email,
                            fullName = "${authData.firstName} ${authData.lastName}",
                            role = authData.role
                        )
                        val loginResponse = LoginResponse(token = authData.token, user = user)
                        
                        // Save token and user data
                        tokenManager.saveToken(authData.token)
                        Timber.d("✅ Token saved for user: ${authData.email}")
                        
                        tokenManager.saveUserData(
                            authData.id,
                            authData.email,
                            "${authData.firstName} ${authData.lastName}",
                            authData.role,
                            null
                        )
                        Timber.d("✅ User data saved: ${authData.email}")
                        
                        Result.success(loginResponse)
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
        firstName: String,
        lastName: String,
        phone: String? = null,
        address: String? = null
    ): Result<LoginResponse> {
        return try {
            val request = RegisterRequest(email, password, firstName, lastName, phone, address)
            val response = api.register(request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        // Convert AuthResponse to LoginResponse
                        val user = User(
                            id = authData.id,
                            email = authData.email,
                            fullName = "${authData.firstName} ${authData.lastName}",
                            role = authData.role
                        )
                        val loginResponse = LoginResponse(token = authData.token, user = user)
                        
                        // Save token and user data
                        tokenManager.saveToken(authData.token)
                        tokenManager.saveUserData(
                            authData.id,
                            authData.email,
                            "${authData.firstName} ${authData.lastName}",
                            authData.role,
                            null
                        )
                        Result.success(loginResponse)
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
            val request = AuthGoogleRequest(idToken = googleToken)
            val response = api.loginWithGoogle(request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val authData = apiResponse.data
                        // Convert AuthResponse to LoginResponse
                        val user = User(
                            id = authData.id,
                            email = authData.email,
                            fullName = "${authData.firstName} ${authData.lastName}",
                            role = authData.role
                        )
                        val loginResponse = LoginResponse(token = authData.token, user = user)
                        
                        // Save token and user data
                        tokenManager.saveToken(authData.token)
                        tokenManager.saveGoogleToken(googleToken)
                        tokenManager.saveUserData(
                            authData.id,
                            authData.email,
                            "${authData.firstName} ${authData.lastName}",
                            authData.role,
                            null
                        )
                        Result.success(loginResponse)
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
    
    suspend fun uploadProfilePicture(base64Image: String): Result<User> {
        return try {
            Timber.d("Uploading profile picture...")
            val authorization = getAuthorizationHeader()
            val request = mapOf(
                "profilePic" to base64Image
            )
            val response = api.uploadProfilePic(authorization, request)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Timber.d("✅ Profile picture uploaded successfully")
                        Result.success(mapBackendUser(apiResponse.data))
                    } else {
                        Result.failure(Exception(apiResponse.message.ifBlank { "Upload failed" }))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("Upload error: $errorBody")
                Result.failure(Exception(errorBody ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Profile picture upload error")
            Result.failure(e)
        }
    }

    suspend fun updateProfile(user: User): Result<User> {
        return try {
            Timber.d("Updating profile for: ${user.email}")
            val authorization = getAuthorizationHeader()
            val (firstName, lastName) = splitFullName(user.fullName)
            val request = BackendUserProfileDto(
                id = user.id,
                email = user.email,
                firstName = firstName,
                lastName = lastName,
                phone = user.phone,
                address = user.address,
                nickname = user.nickname,
                profilePictureUrl = user.profilePic,
                role = user.role
            )
            val response = api.updateProfile(authorization, request)

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val mappedUser = mapBackendUser(apiResponse.data)
                        Timber.d("✅ Profile updated successfully: ${mappedUser.email}")
                        Result.success(mappedUser)
                    } else {
                        Result.failure(Exception(apiResponse.message.ifBlank { "Profile update failed" }))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("Profile update error: $errorBody")
                Result.failure(Exception(errorBody ?: "Profile update failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Profile update error")
            Result.failure(e)
        }
    }
    
    suspend fun getProfile(): Result<User> {
        return try {
            Timber.d("Fetching user profile...")
            val authorization = getAuthorizationHeader()
            val response = api.getUserProfile(authorization)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val mappedUser = mapBackendUser(apiResponse.data)
                        Timber.d("✅ Profile fetched successfully: ${mappedUser.email}")
                        Result.success(mappedUser)
                    } else {
                        Result.failure(Exception(apiResponse.message.ifBlank { "Failed to fetch profile" }))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("Profile fetch error: $errorBody")
                Result.failure(Exception(errorBody ?: "Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Profile fetch error")
            Result.failure(e)
        }
    }
}
