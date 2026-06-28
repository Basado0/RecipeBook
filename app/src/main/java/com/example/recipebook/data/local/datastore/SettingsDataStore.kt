package com.example.recipebook.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val defaultServings: Int = 4,
    val cacheTTLHours: Int = 24,
    val offlineModeEnabled: Boolean = false,
    val autoSaveHistory: Boolean = true,
    val preferredCuisines: List<String> = emptyList()
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DEFAULT_SERVINGS = intPreferencesKey("default_servings")
        val CACHE_TTL_HOURS = intPreferencesKey("cache_ttl_hours")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val AUTO_SAVE_HISTORY = booleanPreferencesKey("auto_save_history")
        val PREFERRED_CUISINES = stringPreferencesKey("preferred_cuisines")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            defaultServings = preferences[PreferencesKeys.DEFAULT_SERVINGS] ?: 4,
            cacheTTLHours = preferences[PreferencesKeys.CACHE_TTL_HOURS] ?: 24,
            offlineModeEnabled = preferences[PreferencesKeys.OFFLINE_MODE] ?: false,
            autoSaveHistory = preferences[PreferencesKeys.AUTO_SAVE_HISTORY] ?: true,
            preferredCuisines = preferences[PreferencesKeys.PREFERRED_CUISINES]?.split(",")
                ?.filter { it.isNotBlank() } ?: emptyList()
        )
    }

    suspend fun updateDefaultServings(servings: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SERVINGS] = servings
        }
    }

    suspend fun updateCacheTTL(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHE_TTL_HOURS] = hours
        }
    }

    suspend fun toggleOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE] = enabled
        }
    }

    suspend fun toggleAutoSaveHistory(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE_HISTORY] = enabled
        }
    }

    suspend fun updatePreferredCuisines(cuisines: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_CUISINES] = cuisines.joinToString(",")
        }
    }
}