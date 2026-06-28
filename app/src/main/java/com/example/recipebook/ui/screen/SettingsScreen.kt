package com.example.recipebook.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipebook.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Default Servings
                item {
                    Text("Default Servings", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = uiState.settings.defaultServings.toFloat(),
                        onValueChange = { viewModel.updateDefaultServings(it.toInt()) },
                        valueRange = 1f..12f,
                        steps = 11
                    )
                    Text(
                        "${uiState.settings.defaultServings} servings",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Cache TTL
                item {
                    Text("Cache Lifetime (hours)", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = uiState.settings.cacheTTLHours.toFloat(),
                        onValueChange = { viewModel.updateCacheTTL(it.toInt()) },
                        valueRange = 1f..72f,
                        steps = 71
                    )
                    Text(
                        "${uiState.settings.cacheTTLHours} hours",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Offline Mode
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Offline Mode", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = uiState.settings.offlineModeEnabled,
                            onCheckedChange = { viewModel.toggleOfflineMode(it) }
                        )
                    }
                }

                // Auto Save History
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto Save History", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = uiState.settings.autoSaveHistory,
                            onCheckedChange = { viewModel.toggleAutoSaveHistory(it) }
                        )
                    }
                }
            }
        }
    }
}