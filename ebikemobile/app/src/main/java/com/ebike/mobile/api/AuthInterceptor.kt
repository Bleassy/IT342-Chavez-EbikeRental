package com.ebike.mobile.api

import android.content.Context
import com.ebike.mobile.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    
    private val tokenManager = TokenManager(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token synchronously for the interceptor
        val token = runBlocking {
            tokenManager.getAccessToken().first()
        }
        
        // Build new request with authorization header
        val requestBuilder = originalRequest.newBuilder()
        
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        requestBuilder.addHeader("Content-Type", "application/json")
        
        val newRequest = requestBuilder.build()
        
        return chain.proceed(newRequest)
    }
}
