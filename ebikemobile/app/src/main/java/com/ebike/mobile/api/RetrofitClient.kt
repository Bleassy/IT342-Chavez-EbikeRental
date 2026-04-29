package com.ebike.mobile.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private var retrofit: Retrofit? = null
    private const val TAG = "RetrofitClient"
    
    // Get base URL from SharedPreferences or default
    fun getBaseUrl(context: Context): String {
        val sharedPref = context.getSharedPreferences("ebike_settings", Context.MODE_PRIVATE)
        val savedUrl = sharedPref.getString("api_base_url", null)
        
        return if (savedUrl != null && savedUrl.isNotEmpty()) {
            Log.d(TAG, "Using saved API URL: $savedUrl")
            savedUrl
        } else {
            // Default - use your computer's actual IP address on the network
            val defaultUrl = "http://192.168.254.109:8083/api/"
            Log.d(TAG, "Using default API URL: $defaultUrl")
            defaultUrl
        }
    }
    
    // Allow setting custom base URL (useful for testing)
    fun setBaseUrl(context: Context, baseUrl: String) {
        val sharedPref = context.getSharedPreferences("ebike_settings", Context.MODE_PRIVATE)
        sharedPref.edit().putString("api_base_url", baseUrl).apply()
        Log.d(TAG, "API URL saved: $baseUrl")
        resetClient() // Reset so next call uses new URL
    }
    
    // Get client for both physical devices and emulator
    fun getClient(context: Context, baseUrl: String? = null): Retrofit {
        if (retrofit == null) {
            val finalUrl = baseUrl ?: getBaseUrl(context)
            
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val gson = GsonBuilder()
                .setLenient()
                .create()
            
            retrofit = Retrofit.Builder()
                .baseUrl(finalUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            
            Log.d(TAG, "Retrofit client created with URL: $finalUrl")
        }
        
        return retrofit!!
    }
    
    fun resetClient() {
        retrofit = null
        Log.d(TAG, "Retrofit client reset")
    }
}
