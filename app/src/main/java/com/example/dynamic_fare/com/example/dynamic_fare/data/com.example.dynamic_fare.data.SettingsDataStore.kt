package com.example.dynamic_fare.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "user_settings")

object SettingsDataStore {
    private val THEME_KEY = stringPreferencesKey("theme_")
    private val LANGUAGE_KEY = stringPreferencesKey("language_")

    fun getTheme(context: Context, userId: String): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(THEME_KEY.name + userId)] ?: false
        }
    }

    suspend fun saveTheme(context: Context, userId: String, isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(THEME_KEY.name + userId)] = isDarkMode
        }
    }

    fun getLanguage(context: Context, userId: String): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(LANGUAGE_KEY.name + userId)] ?: "en"
        }
    }

    suspend fun saveLanguage(context: Context, userId: String, language: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(LANGUAGE_KEY.name + userId)] = language
        }
    }
}
