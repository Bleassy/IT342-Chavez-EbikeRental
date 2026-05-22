package com.ebike.mobile.api

import android.content.Context
import com.ebike.mobile.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor(private val context: Context) : Interceptor {
    
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
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        try {
            // Get token synchronously for the interceptor
            val token = runBlocking {
                try {
                    val t = normalizeToken(tokenManager.getAccessToken().first())
                    Timber.d("🔑 Token retrieved: ${if (t.isNullOrEmpty()) "EMPTY/NULL" else "OK (${t?.length} chars)"}") 
                    t
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error getting token from TokenManager")
                    null
                }
            }
            
            // Build new request with authorization header
            val requestBuilder = originalRequest.newBuilder()
            
            if (token != null && token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
                Timber.d("✅ Authorization header added for: ${originalRequest.url.encodedPath}")
            } else {
                Timber.e("❌ NO TOKEN FOUND - Request to: ${originalRequest.url.encodedPath}")
                Timber.e("🔍 Token status: ${if (token == null) "NULL" else "EMPTY"}")
            }
            
            requestBuilder.addHeader("Content-Type", "application/json")
            
            val newRequest = requestBuilder.build()
            Timber.d("📤 Making ${originalRequest.method} request to: ${originalRequest.url.encodedPath}")
            
            return chain.proceed(newRequest)
        } catch (e: Exception) {
            Timber.e(e, "❌ AuthInterceptor exception")
            throw e
        }
    }
}
