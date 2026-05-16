package com.ebike.mobile.auth

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.ebike.mobile.BuildConfig
import timber.log.Timber
import java.security.MessageDigest

class GoogleSignInHelper(context: Context) {
    
    private val context = context.applicationContext
    private var googleSignInClient: GoogleSignInClient? = null
    
    init {
        initGoogleSignIn()
    }
    
    private fun initGoogleSignIn() {
        try {
            // Log app configuration for debugging
            logAppFingerprint()
            
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            Timber.d("✅ Google Sign-In initialized successfully")
            Timber.d("   Client ID: ${BuildConfig.GOOGLE_CLIENT_ID}")
            Timber.d("   Package: ${context.packageName}")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to initialize Google Sign-In")
        }
    }
    
    /**
     * Log app fingerprint for debugging Google Cloud Console configuration
     */
    private fun logAppFingerprint() {
        try {
            val signatures = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            ).signatures
            
            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA1")
                md.update(signature.toByteArray())
                val fingerprint = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Timber.d("📱 App SHA-1 Fingerprint: $fingerprint")
                Timber.d("   Use this fingerprint in Google Cloud Console")
                Timber.d("   Package Name: ${context.packageName}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get app fingerprint")
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
