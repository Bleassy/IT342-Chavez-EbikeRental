package com.ebike.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val Context.dataStore by preferencesDataStore(name = "ebike_preferences")

class TokenManager(private val context: Context) {
    
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_PROFILE_PIC = stringPreferencesKey("user_profile_pic")
        private val USER_PHONE = stringPreferencesKey("user_phone")
        private val USER_ADDRESS = stringPreferencesKey("user_address")
        private val GOOGLE_TOKEN = stringPreferencesKey("google_token")
    }
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
        // Force read to verify persistence
        val saved = context.dataStore.data.first()[ACCESS_TOKEN]
        Timber.d("🔐 Token saved verification: ${if (saved == token) "✅ CONFIRMED" else "❌ FAILED"}")
    }
    
    suspend fun saveUserData(
        userId: Long,
        email: String,
        fullName: String,
        role: String,
        profilePic: String? = null,
        phone: String? = null,
        address: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId.toString()
            preferences[USER_EMAIL] = email
            preferences[USER_NAME] = fullName
            preferences[USER_ROLE] = role
            profilePic?.let { preferences[USER_PROFILE_PIC] = it }
            phone?.let { preferences[USER_PHONE] = it }
            address?.let { preferences[USER_ADDRESS] = it }
        }
    }
    
    suspend fun saveGoogleToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_TOKEN] = token
        }
    }
    
    fun getAccessToken(): Flow<String?> = context.dataStore.data.map { 
        it[ACCESS_TOKEN] 
    }
    
    fun getUserId(): Flow<String?> = context.dataStore.data.map { 
        it[USER_ID] 
    }
    
    fun getUserEmail(): Flow<String?> = context.dataStore.data.map { 
        it[USER_EMAIL] 
    }
    
    fun getUserName(): Flow<String?> = context.dataStore.data.map { 
        it[USER_NAME] 
    }
    
    fun getUserRole(): Flow<String?> = context.dataStore.data.map { 
        it[USER_ROLE] 
    }
    
    fun getUserProfilePic(): Flow<String?> = context.dataStore.data.map { 
        it[USER_PROFILE_PIC] 
    }

    fun getUserPhone(): Flow<String?> = context.dataStore.data.map {
        it[USER_PHONE]
    }

    fun getUserAddress(): Flow<String?> = context.dataStore.data.map {
        it[USER_ADDRESS]
    }
    
    fun getGoogleToken(): Flow<String?> = context.dataStore.data.map { 
        it[GOOGLE_TOKEN] 
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.first()[ACCESS_TOKEN]?.isNotEmpty() == true
    }
}
