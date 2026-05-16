package com.ebike.mobile.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import timber.log.Timber

object GoogleSignInHandler {
    
    const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    
    fun handleSignInResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: (idToken: String, email: String, name: String, photoUrl: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        Timber.d("🔍 handleSignInResult - requestCode: $requestCode, resultCode: $resultCode, hasData: ${data != null}")
        
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    Timber.d("✅ Received RESULT_OK with data")
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    
                    account?.let {
                        val idToken = it.idToken
                        val email = it.email ?: ""
                        val displayName = it.displayName ?: ""
                        val photoUrl = it.photoUrl?.toString()
                        
                        if (idToken != null) {
                            Timber.d("✅ Google Sign-In successful: $email, displayName: $displayName")
                            onSuccess(idToken, email, displayName, photoUrl)
                        } else {
                            Timber.e("❌ ID Token is null - check if OAuth consent screen was shown")
                            onError("ID Token is null. Please ensure OAuth consent screen was shown.")
                        }
                    } ?: run {
                        Timber.e("❌ Account is null after successful result")
                        onError("Account is null")
                    }
                    
                } catch (e: ApiException) {
                    Timber.e(e, "❌ Google Sign-In failed with ApiException code: ${e.statusCode}")
                    val errorMessage = when (e.statusCode) {
                        12500 -> "Google Play Services not available. Update Google Play Services."
                        12501 -> "Sign in cancelled by user"
                        12502 -> "Google Play Services update required"
                        10 -> "The calling package was not recognized. Check app package name and SHA-1 fingerprint in Google Cloud Console."
                        else -> "Google Sign-In failed (Code: ${e.statusCode}): ${e.message}"
                    }
                    Timber.e("📋 Error details: $errorMessage")
                    onError(errorMessage)
                } catch (e: Exception) {
                    Timber.e(e, "❌ Unexpected error during Google Sign-In")
                    onError(e.message ?: "Unexpected error")
                }
            } else {
                val errorReason = when {
                    resultCode != Activity.RESULT_OK -> "resultCode != RESULT_OK (got: $resultCode)"
                    data == null -> "data is null"
                    else -> "Unknown reason"
                }
                Timber.e("❌ Sign-In result not OK: $errorReason")
                Timber.e("📋 This usually means:")
                Timber.e("   1. User cancelled the sign-in dialog")
                Timber.e("   2. App SHA-1 fingerprint not registered in Google Cloud Console")
                Timber.e("   3. Package name doesn't match Google Cloud Console configuration")
                Timber.e("   4. OAuth Client ID not properly associated with Android app")
                onError("Sign-In cancelled or not configured properly. See logs for details.")
            }
        }
    }
}
