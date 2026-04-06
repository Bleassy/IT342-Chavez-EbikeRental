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
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    
                    account?.let {
                        val idToken = it.idToken
                        val email = it.email ?: ""
                        val displayName = it.displayName ?: ""
                        val photoUrl = it.photoUrl?.toString()
                        
                        if (idToken != null) {
                            Timber.d("Google Sign-In successful: $email")
                            onSuccess(idToken, email, displayName, photoUrl)
                        } else {
                            onError("ID Token is null")
                        }
                    } ?: onError("Account is null")
                    
                } catch (e: ApiException) {
                    Timber.e(e, "Google Sign-In failed with code: ${e.statusCode}")
                    val errorMessage = when (e.statusCode) {
                        12500 -> "Google Play Services not available"
                        12501 -> "Sign in cancelled by user"
                        12502 -> "Google Play Services update required"
                        else -> "Google Sign-In failed: ${e.message}"
                    }
                    onError(errorMessage)
                } catch (e: Exception) {
                    Timber.e(e, "Unexpected error during Google Sign-In")
                    onError(e.message ?: "Unexpected error")
                }
            } else {
                onError("Sign-In cancelled or failed")
            }
        }
    }
}
