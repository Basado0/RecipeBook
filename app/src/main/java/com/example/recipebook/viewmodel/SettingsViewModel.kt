package com.example.recipebook.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.local.datastore.AppSettings
import com.example.recipebook.data.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsDataStore.settingsFlow
        .map { settings ->
            SettingsUiState(
                settings = settings,
                isLoading = false
            )
        }
        .catch { e ->
            emit(SettingsUiState(error = "Failed to load settings: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun updateDefaultServings(servings: Int) {
        viewModelScope.launch {
            try {
                settingsDataStore.updateDefaultServings(servings)
            } catch (e: Exception) {

            }
        }
    }

    fun updateCacheTTL(hours: Int) {
        viewModelScope.launch {
            try {
                settingsDataStore.updateCacheTTL(hours)
            } catch (e: Exception) {

            }
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsDataStore.toggleOfflineMode(enabled)
            } catch (e: Exception) {

            }
        }
    }

    fun toggleAutoSaveHistory(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsDataStore.toggleAutoSaveHistory(enabled)
            } catch (e: Exception) {

            }
        }
    }

    fun updatePreferredCuisines(cuisines: List<String>) {
        viewModelScope.launch {
            try {
                settingsDataStore.updatePreferredCuisines(cuisines)
            } catch (e: Exception) {

            }
        }
    }
}