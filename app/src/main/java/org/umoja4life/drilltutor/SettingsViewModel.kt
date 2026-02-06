package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // Access the repositories via the Singleton (Service Locator Pattern)
    private val settingsRepo = Environment.settings
    private val flashcardRepo = Environment.flashcards

    // --- EXPOSED STATE ---
    // We convert the repository flows into "Hot" flows that the UI can safely watch.
    // "stateIn" ensures they only update when the UI is actually looking at them.

    val currentLanguage: StateFlow<String> = settingsRepo.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "tr")

    val currentTopic: StateFlow<String> = settingsRepo.topic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_TOPIC)

    val currentSource: StateFlow<FlashcardSource> = settingsRepo.source
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SOURCE)

    val currentSelector: StateFlow<SelectorType> = settingsRepo.selector
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SELECTOR)

    val currentSize: StateFlow<Int> = settingsRepo.groupSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SIZE)

    val currentSide: StateFlow<CardSide> = settingsRepo.cardSide
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SIDE)

    // --- DYNAMIC DATA ---

    // Expose the loaded topics to the UI
    // We use a custom getter so it always retrieves the latest list from the Repo.
    val availableTopics: List<String>
        get() = flashcardRepo.getTopics(FlashcardSource.VOCABULARY)

    // --- ACTIONS ---
    // The UI calls these methods when the user changes a setting.

    // When Language changes, we must reload the Data.
    fun setLanguage(lang: String) {
        settingsRepo.setLanguage(lang)         // Save Preference
        viewModelScope.launch {
            flashcardRepo.loadFlashcardData(lang)     // Load Data & Build Topics
        }
    }
    fun setTopic(topic: String)   = settingsRepo.setTopic(topic)
    fun setSource(source: FlashcardSource) = settingsRepo.setSource(source)
    fun setSelector(selector: SelectorType) = settingsRepo.setSelector(selector)
    fun setGroupSize(size: Int)   = settingsRepo.setGroupSize(size)
    fun setCardSide(side: CardSide) = settingsRepo.setCardSide(side)

    // Helper to get valid options for the UI (Dropdowns)
    val availableSources = FlashcardSource.entries
    val availableSelectors = SelectorType.entries
    val availableSides = CardSide.entries
    val availableSizes = SettingsRepository.VALID_GROUP_SIZES

    val availableLanguages = listOf("en", "sw", "tr")
}