package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    val availableLanguages = listOf("en", "sw", "tr")

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
    fun setLanguage(newVal: String) {
        // RESET LOGIC:
        // Instead of 'it.copy()' (which keeps old junk), we create a FRESH State object.
        // Because SettingState has default values in its constructor,
        // this resets topic, source, groupSize, etc. to their defaults.
        val resetState = SettingState(language = newVal)

        // Save this fresh state to persistence
        updateSettings { resetState }

        // Trigger the data load (using global scope to prevent cancellation)
        Environment.scope.launch {
            flashcardRepo.loadFlashcardData(newVal)
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