package com.ebike.mobile.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.ebike.mobile.BuildConfig
import timber.log.Timber

class GoogleSignInHelper(context: Context) {
    
    private val context = context.applicationContext
    private var googleSignInClient: GoogleSignInClient? = null
    
    init {
        initGoogleSignIn()
    }
    
    private fun initGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            Timber.d("Google Sign-In initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Google Sign-In")
        }
    }
    
    fun getGoogleSignInClient(): GoogleSignInClient? {
        if (googleSignInClient == null) {
            initGoogleSignIn()
        }
        return googleSignInClient
    }
    
    fun getSignInIntent() = googleSignInClient?.signInIntent
    
    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInResult {
        return try {
            val account = task.result
            GoogleSignInResult.Success(
                idToken = account.idToken ?: "",
                email = account.email ?: "",
                displayName = account.displayName ?: "",
                photoUrl = account.photoUrl?.toString()
            )
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed")
            GoogleSignInResult.Error(e.message ?: "Google Sign-In failed")
        }
    }
    
    fun signOut() {
        try {
            googleSignInClient?.signOut()
            Timber.d("Google Sign-Out successful")
        } catch (e: Exception) {
            Timber.e(e, "Failed to sign out from Google")
        }
    }
}

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val email: String,
        val displayName: String,
        val photoUrl: String?
    ) : GoogleSignInResult()
    
    data class Error(val message: String) : GoogleSignInResult()
}
