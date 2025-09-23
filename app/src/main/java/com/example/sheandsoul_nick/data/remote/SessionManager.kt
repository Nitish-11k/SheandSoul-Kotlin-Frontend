// app/src/main/java/com/example/sheandsoul_nick/data/SessionManager.kt

package com.example.sheandsoul_nick.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        // Define the key for storing the auth token
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val IS_PROFILE_COMPLETE = booleanPreferencesKey("is_profile_complete")
    }

    // Function to save the auth token to DataStore
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }
    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN] = token
        }
    }
    suspend fun saveProfileCompleteStatus(isComplete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PROFILE_COMPLETE] = isComplete
        }
    }
    val fcmTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[FCM_TOKEN]
        }
    val isProfileCompleteFlow: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_PROFILE_COMPLETE]
        }

    // A flow to observe the auth token. It will emit null if no token is stored.
    val authTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    // Function to clear the auth token on logout
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(IS_PROFILE_COMPLETE)
        }
    }
}