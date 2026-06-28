package com.example.recipebook.viewmodel.localRecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.local.UserEntities.UserColletcions.CollectionRepository
import com.example.recipebook.models.RecipeCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionsUiState(
    val collections: List<RecipeCollection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val newCollectionName: String = "",
    val newCollectionDescription: String = "",
    val newCollectionIcon: String = "📖"
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val collectionsFlow: Flow<List<RecipeCollection>> =
        collectionRepository.observeAllCollections()

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _showCreateDialog = MutableStateFlow(false)
    private val _newCollectionName = MutableStateFlow("")
    private val _newCollectionDescription = MutableStateFlow("")
    private val _newCollectionIcon = MutableStateFlow("📖")

    private val dialogState: Flow<DialogState> = combine(
        _showCreateDialog,
        _newCollectionName,
        _newCollectionDescription,
        _newCollectionIcon
    ) { showDialog, name, description, icon ->
        DialogState(showDialog, name, description, icon)
    }


    val uiState: StateFlow<CollectionsUiState> = combine(
        collectionsFlow,
        _isLoading,
        _error,
        dialogState
    ) { collections, isLoading, error, dialog ->
        CollectionsUiState(
            collections = collections,
            isLoading = isLoading,
            error = error,
            showCreateDialog = dialog.showDialog,
            newCollectionName = dialog.name,
            newCollectionDescription = dialog.description,
            newCollectionIcon = dialog.icon
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollectionsUiState()
    )

    init {
        viewModelScope.launch {
            collectionsFlow.first()
            _isLoading.value = false
        }
    }

    fun showCreateDialog() {
        _showCreateDialog.value = true
    }

    fun hideCreateDialog() {
        _showCreateDialog.value = false
        _newCollectionName.value = ""
        _newCollectionDescription.value = ""
        _newCollectionIcon.value = "📖"
    }

    fun updateNewCollectionName(name: String) {
        _newCollectionName.value = name
    }

    fun updateNewCollectionDescription(description: String) {
        _newCollectionDescription.value = description
    }

    fun updateNewCollectionIcon(icon: String) {
        _newCollectionIcon.value = icon
    }

    fun createCollection() {
        val name = _newCollectionName.value
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                collectionRepository.createCollection(
                    name = name,
                    description = _newCollectionDescription.value.ifBlank { null },
                    icon = _newCollectionIcon.value
                )
                hideCreateDialog()
            } catch (e: Exception) {
                _error.value = "Failed to create collection"
            }
        }
    }

    fun addRecipeToCollection(collectionId: Int, recipeId: Int) {
        viewModelScope.launch {
            try {
                val added = collectionRepository.addRecipeToCollection(
                    collectionId = collectionId,
                    recipeId = recipeId
                )
                if (!added) {
                    _error.value = "Recipe already in collection"
                }
            } catch (e: Exception) {
                _error.value = "Failed to add recipe"
            }
        }
    }

    fun deleteCollection(collectionId: Int) {
        viewModelScope.launch {
            try {
                collectionRepository.deleteCollection(collectionId)
            } catch (e: Exception) {
                _error.value = "Failed to delete collection"
            }
        }
    }

    fun removeRecipeFromCollection(collectionId: Int, recipeId: Int) {
        viewModelScope.launch {
            try {
                collectionRepository.removeRecipeFromCollection(collectionId, recipeId)
            } catch (e: Exception) {
                _error.value = "Failed to remove recipe"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private data class DialogState(
        val showDialog: Boolean = false,
        val name: String = "",
        val description: String = "",
        val icon: String = "📖"
    )
}