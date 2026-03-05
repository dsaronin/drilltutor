package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    // Access the repositories via the Singleton (Service Locator Pattern)
    private val settingsRepo = Environment.settings
    private val flashcardRepo = Environment.flashcards

    // --- EXPOSED STATE ---
    // We convert the SettingState flow into a StateFlow that the UI can safely watch.
    val settings: StateFlow<SettingState> = settingsRepo.settingState

    // --- DYNAMIC DATA ---

    // Expose the loaded topics to the UI
    // We use a custom getter so it always retrieves the latest list from the Repo.
    val availableTopics: List<String>
        get() = flashcardRepo.getTopics(FlashcardSource.VOCABULARY)

    private val _availableLanguages = MutableStateFlow<List<String>>(emptyList())
    val availableLanguages: StateFlow<List<String>> = _availableLanguages.asStateFlow()

    // --- ACTIONS ---
    // The UI calls these methods when the user changes a setting.

    // When Language changes, we must reload the Data.

    // *******************************************************
    // Helper to get valid options for the UI (Dropdowns)
    // *******************************************************
    val availableSources = FlashcardSource.getSourceList()
    val availableSelectors = SelectorType.entries
    val availableSides = CardSide.entries
    val availableSizes = SettingsRepository.VALID_GROUP_SIZES

    init {
        viewModelScope.launch {
            _availableLanguages.value = flashcardRepo.getAvailableLanguages()
        }
    }


    /**
     * availableSelections
     * Resolves the subtype handler for the current source and fetches its specific topics.
     */
    val availableSelections: List<String>
        get() {
            val source = settings.value.source
            val handler = FlashcardTypeSelection.selectCardType(source)
            return handler.getTopics()
        }

    // *******************************************************

    // *******************************************************
    // HELPERS
    // *******************************************************
    /**
     * isSelectionVisible
     * The Selection dropdown is hidden for Vocabulary and Dictionary sources.
     */
    val isSelectionVisible: Boolean
        get() {
            val s = settings.value.source
            return s != FlashcardSource.VOCABULARY && s != FlashcardSource.DICTIONARY
        }

    fun setTopic(newVal: String)     = updateSettings { it.copy(topic = newVal) }
    fun setSource(newVal: FlashcardSource) = updateSettings {
        it.copy(
            source = newVal,
            entryKey = SettingsRepository.DEFAULT_ITEMKEY // Reset selection on source change
        )
    }
    fun setEntryKey(newVal: String)  = updateSettings { it.copy(entryKey = newVal) }
    // Inside SettingState data class
    fun setSelector(newVal: SelectorType)  = updateSettings { it.copy(selector = newVal) }
    fun setGroupSize(newVal: Int)    = updateSettings { it.copy(groupSize = newVal) }
    fun setCardSide(newVal: CardSide) = updateSettings { it.copy(cardSide = newVal) }
    fun setShowExamples(newVal: Boolean) = updateSettings { it.copy(showExamples = newVal) }
    fun setLanguage(newVal: String) {
        // Trigger the data load (using global scope to prevent cancellation)
        // Note, this will also reset Settings, PlayerState.
        Environment.scope.launch {
            flashcardRepo.executeDataLoad(
                Environment.storage.loadStorageState().storageUri,
                newVal
            )
        }
    }

    /**
     * updateSettings
     */
    private fun updateSettings(modifier: (SettingState) -> SettingState) {
        val current = settingsRepo.settingState.value
        val new = modifier(current)
        settingsRepo.updateSettings(new)
    }
}