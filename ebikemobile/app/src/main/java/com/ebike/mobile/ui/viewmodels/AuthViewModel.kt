package com.ebike.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebike.mobile.auth.GoogleSignInHelper
import com.ebike.mobile.data.local.TokenManager
import com.ebike.mobile.data.models.LoginResponse
import com.ebike.mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(private val context: Context) : ViewModel() {
    
    private val repository = AuthRepository(context)
    private val tokenManager = TokenManager(context)
    private val googleSignInHelper by lazy { GoogleSignInHelper(context) }

    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _loginResult = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginResult: StateFlow<Result<LoginResponse>?> = _loginResult
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _googleSignInIntent = MutableStateFlow<android.content.Intent?>(null)
    val googleSignInIntent: StateFlow<android.content.Intent?> = _googleSignInIntent
    
    private val _currentUser = MutableStateFlow<com.ebike.mobile.data.models.User?>(null)
    val currentUser: StateFlow<com.ebike.mobile.data.models.User?> = _currentUser

    private fun mergeUserData(
        existing: com.ebike.mobile.data.models.User?,
        incoming: com.ebike.mobile.data.models.User
    ): com.ebike.mobile.data.models.User {
        val incomingFullName = runCatching { incoming.fullName }.getOrNull()
        val incomingEmail = runCatching { incoming.email }.getOrNull()
        val incomingRole = runCatching { incoming.role }.getOrNull()

        return incoming.copy(
            fullName = incomingFullName?.takeUnless { it.isBlank() }
                ?: existing?.fullName
                ?: "",
            email = incomingEmail?.takeUnless { it.isBlank() }
                ?: existing?.email
                ?: "",
            phone = incoming.phone ?: existing?.phone,
            address = incoming.address ?: existing?.address,
            profilePic = incoming.profilePic ?: existing?.profilePic,
            role = incomingRole?.takeUnless { it.isBlank() }
                ?: existing?.role
                ?: "CUSTOMER"
        )
    }
    
    init {
        checkLoginStatus()
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Timber.d("Login attempt for: $email")
            
            val result = repository.login(email, password)
            _loginResult.value = result
            
            if (result.isSuccess) {
                result.getOrNull()?.user?.let { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    Timber.d("✓ Login successful for: ${user.email}, fullName: ${user.fullName}")
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
                Timber.e("✗ Login failed: ${result.exceptionOrNull()?.message}")
            }
            
            _isLoading.value = false
        }
    }
    
    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String? = null,
        address: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.register(email, password, firstName, lastName, phone, address)
            _loginResult.value = result
            
            if (result.isSuccess) {
                _isLoggedIn.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
            
            _isLoading.value = false
        }
    }
    
    fun initiateGoogleSignIn() {
        try {
            val signInIntent = googleSignInHelper.getSignInIntent()
            _googleSignInIntent.value = signInIntent
            Timber.d("Google Sign-In intent created")
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initiate Google Sign-In: ${e.message}"
            Timber.e(e, "Failed to initiate Google Sign-In")
        }
    }
    
    fun handleGoogleSignInResult(
        idToken: String,
        email: String,
        displayName: String,
        photoUrl: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Timber.d("Google login attempt for: $email")
            
            val result = repository.loginWithGoogle(idToken)
            _loginResult.value = result
            
            if (result.isSuccess) {
                result.getOrNull()?.user?.let { user ->
                    val updatedUser = user.copy(
                        email = email,
                        fullName = displayName,
                        profilePic = photoUrl
                    )
                    _currentUser.value = updatedUser
                    _isLoggedIn.value = true
                    Timber.d("✓ Google login successful for: $email, fullName: $displayName")
                    Timber.d("✓ User set to: ${_currentUser.value?.email}")
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Google login failed"
                Timber.e("✗ Google login failed: ${result.exceptionOrNull()?.message}")
            }
            
            _isLoading.value = false
            Timber.d("isLoggedIn now: ${_isLoggedIn.value}")
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            googleSignInHelper.signOut()
            _isLoggedIn.value = false
            _currentUser.value = null
            _loginResult.value = null
            _errorMessage.value = null
            Timber.d("Logout successful")
        }
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                if (tokenManager.isLoggedIn()) {
                    // Load saved user data
                    val userId = tokenManager.getUserId().first() ?: return@launch
                    val email = tokenManager.getUserEmail().first() ?: return@launch
                    val fullName = tokenManager.getUserName().first() ?: return@launch
                    val role = tokenManager.getUserRole().first() ?: "CUSTOMER"
                    val profilePic = tokenManager.getUserProfilePic().first()
                    val phone = tokenManager.getUserPhone().first()
                    val address = tokenManager.getUserAddress().first()
                    
                    val user = com.ebike.mobile.data.models.User(
                        id = userId.toLongOrNull() ?: return@launch,
                        email = email,
                        fullName = fullName,
                        role = role,
                        profilePic = profilePic,
                        phone = phone,
                        address = address
                    )
                    
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    Timber.d("Restored login session for: $email")
                } else {
                    Timber.d("No active login session")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking login status")
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun uploadProfilePicture(base64Image: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Timber.d("Uploading profile picture...")
            
            val result = repository.uploadProfilePicture(base64Image)
            
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    val mergedUser = mergeUserData(_currentUser.value, user)
                    _currentUser.value = mergedUser
                    tokenManager.saveUserData(
                        mergedUser.id,
                        mergedUser.email,
                        mergedUser.fullName,
                        mergedUser.role,
                        mergedUser.profilePic,
                        mergedUser.phone,
                        mergedUser.address
                    )
                    Timber.d("✅ Profile picture updated successfully")
                    refreshUserProfile()
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Upload failed"
                Timber.e("❌ Profile picture upload failed: ${result.exceptionOrNull()?.message}")
            }
            
            _isLoading.value = false
        }
    }
    
    fun refreshUserProfile() {
        viewModelScope.launch {
            Timber.d("Refreshing user profile...")
            try {
                val result = repository.getProfile()
                if (result.isSuccess) {
                    result.getOrNull()?.let { user ->
                        val mergedUser = mergeUserData(_currentUser.value, user)
                        _currentUser.value = mergedUser
                        tokenManager.saveUserData(
                            mergedUser.id,
                            mergedUser.email,
                            mergedUser.fullName,
                            mergedUser.role,
                            mergedUser.profilePic,
                            mergedUser.phone,
                            mergedUser.address
                        )
                        Timber.d("✅ User profile refreshed successfully")
                    }
                } else {
                    Timber.w("Failed to refresh profile: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing profile")
            }
        }
    }

    fun updateProfile(
        fullName: String,
        phone: String,
        address: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val existingUser = _currentUser.value
            if (existingUser == null) {
                _errorMessage.value = "No profile loaded"
                _isLoading.value = false
                onComplete(false)
                return@launch
            }

            val requestUser = existingUser.copy(
                fullName = fullName.ifBlank { existingUser.fullName },
                phone = phone.ifBlank { null },
                address = address.ifBlank { null }
            )

            val result = repository.updateProfile(requestUser)
            if (result.isSuccess) {
                result.getOrNull()?.let { updatedUser ->
                    val mergedUser = mergeUserData(existingUser, updatedUser)
                    _currentUser.value = mergedUser
                    tokenManager.saveUserData(
                        mergedUser.id,
                        mergedUser.email,
                        mergedUser.fullName,
                        mergedUser.role,
                        mergedUser.profilePic,
                        mergedUser.phone,
                        mergedUser.address
                    )
                    Timber.d("✅ Profile updated in ViewModel")
                    onComplete(true)
                } ?: run {
                    _errorMessage.value = "Empty profile update response"
                    onComplete(false)
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Profile update failed"
                onComplete(false)
            }

            _isLoading.value = false
        }
    }
}
