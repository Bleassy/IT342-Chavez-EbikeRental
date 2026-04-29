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
    
    init {
        checkLoginStatus()
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.login(email, password)
            _loginResult.value = result
            
            if (result.isSuccess) {
                _isLoggedIn.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
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
            
            val result = repository.loginWithGoogle(idToken)
            _loginResult.value = result
            
            if (result.isSuccess) {
                _isLoggedIn.value = true
                Timber.d("Google login successful for: $email")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Google login failed"
                Timber.e("Google login failed: ${result.exceptionOrNull()?.message}")
            }
            
            _isLoading.value = false
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            googleSignInHelper.signOut()
            _isLoggedIn.value = false
            _loginResult.value = null
            _errorMessage.value = null
            Timber.d("Logout successful")
        }
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                _isLoggedIn.value = tokenManager.isLoggedIn()
            } catch (e: Exception) {
                Timber.e(e, "Error checking login status")
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
