package com.ebike.mobile.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * Utility for managing API server configuration
 */
object ApiConfig {
    private const val TAG = "ApiConfig"
    private const val PREF_NAME = "ebike_settings"
    private const val KEY_API_URL = "api_base_url"
    
    // Common server URLs
    val URLS = mapOf(
        "Local (Physical Device)" to "http://192.168.254.105:8083/api/",
        "Emulator" to "http://10.0.2.2:8083/api/",
        "Production" to "http://your-production-domain.com/api/",
        "Custom" to ""
    )
    
    /**
     * Get current API base URL
     */
    fun getApiUrl(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = sharedPref.getString(KEY_API_URL, null)
        
        return if (saved != null && saved.isNotEmpty()) {
            Log.d(TAG, "Using saved API URL: $saved")
            saved
        } else {
            val default = "http://192.168.254.105:8083/api/"
            Log.d(TAG, "Using default API URL: $default")
            default
        }
    }
    
    /**
     * Save API URL to SharedPreferences
     */
    fun setApiUrl(context: Context, url: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString(KEY_API_URL, url)
            apply()
        }
        Log.d(TAG, "Saved API URL: $url")
        RetrofitClient.resetClient()
    }
    
    /**
     * Get device IP address (for reference/debugging)
     */
    fun getDeviceIp(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            if (network == null) {
                return "No network connected"
            }
            
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                val addresses = java.net.InetAddress.getAllByName(java.net.InetAddress.getLocalHost().hostName)
                val ipAddress = addresses.firstOrNull { !it.isLoopbackAddress }?.hostAddress
                ipAddress ?: "Unknown IP"
            } else {
                "No internet capability"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device IP", e)
            "Error: ${e.message}"
        }
    }
    
    /**
     * Get list of preset URLs
     */
    fun getPresetUrls(): List<String> {
        return URLS.keys.toList()
    }
    
    /**
     * Get URL for preset
     */
    fun getUrlForPreset(preset: String): String {
        return URLS[preset] ?: ""
    }
}
