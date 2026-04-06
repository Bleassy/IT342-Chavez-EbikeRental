package com.ebike.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ebike.mobile.auth.GoogleSignInHandler
import com.ebike.mobile.ui.screens.AppNavigation
import com.ebike.mobile.ui.theme.EBikeMobileTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    
    var googleSignInCallback: ((idToken: String, email: String, displayName: String, photoUrl: String?) -> Unit)? = null
    var googleSignInErrorCallback: ((message: String) -> Unit)? = null
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        GoogleSignInHandler.handleSignInResult(
            requestCode = GoogleSignInHandler.GOOGLE_SIGN_IN_REQUEST_CODE,
            resultCode = result.resultCode,
            data = result.data,
            onSuccess = { idToken, email, name, photoUrl ->
                googleSignInCallback?.invoke(idToken, email, name, photoUrl)
            },
            onError = { message ->
                googleSignInErrorCallback?.invoke(message)
            }
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        setContent {
            EBikeMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(this@MainActivity, googleSignInLauncher)
                }
            }
        }
    }
}
