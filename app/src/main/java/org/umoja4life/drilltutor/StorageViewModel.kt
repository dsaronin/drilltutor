package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorageViewModel : ViewModel() {

    private val storageRepo = Environment.storage

    // --- EXPOSED STATE ---
    private val _storageState = MutableStateFlow(StorageState())
    val storageState: StateFlow<StorageState> = _storageState.asStateFlow()

    init {
        // Load the initial state from persistence when the ViewModel is created
        viewModelScope.launch {
            _storageState.value = storageRepo.loadStorageState()
        }
    }

    // --- ACTIONS ---

    /**
     * Updates the storage URI and persists it to the DataStore.
     * An empty string implies reverting to the default internal assets.
     */
    fun updateStorageUri(newUri: String) {
        val newState = _storageState.value.copy(storageUri = newUri)
        _storageState.value = newState // Instant UI update

        viewModelScope.launch {
            storageRepo.saveStorageState(newState) // Async persistence
        }

        // Trigger the universal data load (using global scope to prevent cancellation)
        Environment.scope.launch {
            Environment.flashcards.executeDataLoad(
                newUri,
                Environment.settings.validLanguage()
            )
        }
    }
}