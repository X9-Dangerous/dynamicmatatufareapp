package com.example.dynamic_fare.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object UserSessionDataStore {
    private const val DATASTORE_NAME = "user_session_prefs"
    private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)
    private val USER_EMAIL_KEY = stringPreferencesKey("logged_in_user_email")
    private val USER_ROLE_KEY = stringPreferencesKey("logged_in_user_role")

    suspend fun saveUserEmail(context: Context, email: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL_KEY] = email
        }
    }

    suspend fun saveUserRole(context: Context, role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ROLE_KEY] = role
        }
    }

    fun getUserEmail(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[USER_EMAIL_KEY]
        }

    fun getUserRole(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[USER_ROLE_KEY]
        }

    suspend fun clearUserEmail(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USER_EMAIL_KEY)
            prefs.remove(USER_ROLE_KEY)
        }
    }
}
