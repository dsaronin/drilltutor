package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrillViewModel : ViewModel() {

    // --- UI STATE ---
    // The specific card content to display
    private val _currentCard = MutableStateFlow(FlashcardData("", ""))
    val currentCard: StateFlow<FlashcardData> = _currentCard.asStateFlow()

    // The Loading Flag (True = Show Spinner, False = Show Card)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- LOGIC ENGINE ---
    // Nullable because it is invalid while data is loading
    private var flashManager: FlashManager? = null

    init {
        monitorRepository()
    }

    private fun monitorRepository() {
        viewModelScope.launch {
            // OBSERVE: Listen to the Singleton Repository's status stream.
            // This fires on App Start AND when Settings changes the language.
            Environment.flashcards.dataState.collect { status ->
                when (status) {
                    DataStatus.Loading -> {
                        _isLoading.value = true
                    }
                    DataStatus.Ready -> {
                        rebuildManager()
                        _isLoading.value = false
                    }
                    DataStatus.Idle -> {
                        // Do nothing, waiting for trigger
                    }
                }
            }
        }
    }

    private suspend fun rebuildManager() {
        Environment.logInfo("VM: (Re)Building FlashManager...")

        val playState = Environment.playerState.loadPlayerState()

        // 1. Instantiate the Logic Engine with fresh data
        flashManager = FlashManager(
            Environment.settings,
            playState
        )

        // 2. Push initial state to UI
        _currentCard.value = flashManager?.currentCard() ?: FlashcardData()
    }
}