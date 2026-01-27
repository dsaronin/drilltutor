package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel : ViewModel() {

    // Access the repository via the Singleton (Service Locator Pattern)
    private val repository = Environment.settings

    // --- EXPOSED STATE ---
    // We convert the repository flows into "Hot" flows that the UI can safely watch.
    // "stateIn" ensures they only update when the UI is actually looking at them.

    val currentLanguage: StateFlow<String> = repository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val currentTopic: StateFlow<String> = repository.topic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_TOPIC)

    val currentSource: StateFlow<FlashcardSource> = repository.source
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SOURCE)

    val currentSelector: StateFlow<SelectorType> = repository.selector
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SELECTOR)

    val currentSize: StateFlow<Int> = repository.groupSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SIZE)

    val currentSide: StateFlow<CardSide> = repository.cardSide
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SIDE)

    // --- ACTIONS ---
    // The UI calls these methods when the user changes a setting.

    fun setLanguage(lang: String) = repository.setLanguage(lang)
    fun setTopic(topic: String)   = repository.setTopic(topic)
    fun setSource(source: FlashcardSource) = repository.setSource(source)
    fun setSelector(selector: SelectorType) = repository.setSelector(selector)
    fun setGroupSize(size: Int)   = repository.setGroupSize(size)
    fun setCardSide(side: CardSide) = repository.setCardSide(side)

    // Helper to get valid options for the UI (Dropdowns)
    val availableSources = FlashcardSource.entries
    val availableSelectors = SelectorType.entries
    val availableSides = CardSide.entries
    val availableSizes = SettingsRepository.VALID_GROUP_SIZES

    val availableLanguages = listOf("en", "sw", "tr")
}