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
    val availableSources = FlashcardSource.entries
    val availableSelectors = SelectorType.entries
    val availableSides = CardSide.entries
    val availableSizes = SettingsRepository.VALID_GROUP_SIZES
    val availableLanguages = listOf("en", "sw", "tr")
    // *******************************************************

    // *******************************************************
    // HELPERS
    // *******************************************************
    fun setTopic(newVal: String)     = updateSettings { it.copy(topic = newVal) }
    fun setSource(newVal: FlashcardSource) = updateSettings { it.copy(source = newVal) }
    fun setSelector(newVal: SelectorType)  = updateSettings { it.copy(selector = newVal) }
    fun setGroupSize(newVal: Int)    = updateSettings { it.copy(groupSize = newVal) }
    fun setCardSide(newVal: CardSide) = updateSettings { it.copy(cardSide = newVal) }
   fun setLanguage(newVal: String) {
        updateSettings { it.copy(language = newVal) }         // Save Preference

        // FIRE-AND-FORGET: Trigger the reload.
        // DrillViewModel will detect the 'Ready' signal and rebuild itself.
        viewModelScope.launch {
            flashcardRepo.loadFlashcardData(newVal)     // Load Data & Build Topics
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