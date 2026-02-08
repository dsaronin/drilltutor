package org.umoja4life.drilltutor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ****************************************************************
// Unified State Container
@Serializable
data class SettingState(
    val language: String = "tr",
    val topic: String = SettingsRepository.DEFAULT_TOPIC,
    val source: FlashcardSource = SettingsRepository.DEFAULT_SOURCE,
    val selector: SelectorType = SettingsRepository.DEFAULT_SELECTOR,
    val groupSize: Int = SettingsRepository.DEFAULT_SIZE,
    val cardSide: CardSide = SettingsRepository.DEFAULT_SIDE
)

// ****************************************************************
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
// ****************************************************************

class SettingsRepository(context: Context) {

    private val TAG = "SettingsRepo"

    // ****************************************************************
    companion object {
        // Defaults using Type Safety
        const val DEFAULT_TOPIC = "default"
        val DEFAULT_SOURCE = FlashcardSource.VOCABULARY
        val DEFAULT_SELECTOR = SelectorType.ORDERED
        const val DEFAULT_SIZE = 5
        val DEFAULT_SIDE = CardSide.FRONT

        // Group sizes are numbers, so a simple List is fine here
        val VALID_GROUP_SIZES = listOf(5, 10, 15, 25, 50)
    }
    // ****************************************************************

    // --- INFRASTRUCTURE ---
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore: DataStore<Preferences> = context.dataStore

    // --- PERSISTENCE KEYS ---
    // Single key for the entire state object
    private val KEY_SETTINGS_STATE = stringPreferencesKey("settings_state_json")
    // JSON Configuration
    private val json = Json { ignoreUnknownKeys = true }

    // --- STATE FLOWS (Exposing Enums, not Strings) ---

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _topic = MutableStateFlow(DEFAULT_TOPIC)
    val topic: StateFlow<String> = _topic.asStateFlow()

    private val _source = MutableStateFlow(DEFAULT_SOURCE)
    val source: StateFlow<FlashcardSource> = _source.asStateFlow()

    private val _selector = MutableStateFlow(DEFAULT_SELECTOR)
    val selector: StateFlow<SelectorType> = _selector.asStateFlow()

    private val _groupSize = MutableStateFlow(DEFAULT_SIZE)
    val groupSize: StateFlow<Int> = _groupSize.asStateFlow()

    private val _cardSide = MutableStateFlow(DEFAULT_SIDE)
    val cardSide: StateFlow<CardSide> = _cardSide.asStateFlow()

    init {
        loadSettingState()
    }

    /**
     * loadSettingState
     * Fetches stored JSON, decodes to object.
     * Returns default instance if missing or error.
     */
    private fun loadSettingState() {
        scope.launch {
            val prefs = dataStore.data.first()
            val jsonString = prefs[KEY_SETTINGS_STATE]

            val state = if (jsonString.isNullOrEmpty()) {
                Environment.logInfo("$TAG: No saved state found. Using Defaults.")
                SettingState() // Return Defaults
            } else {
                try {
                    json.decodeFromString<SettingState>(jsonString)
                } catch (e: Exception) {
                    Environment.logError("$TAG: Load failed. ${e.message}")
                    SettingState()
                }
            }

            Environment.logInfo("$TAG: Loaded State: Topic=${state.topic}, Source=${state.source.sourceName}")

            // --- THE BRIDGE ---
            // Push values into the legacy flows so existing consumers work
            _language.value  = state.language
            _topic.value     = state.topic
            _source.value    = state.source
            _selector.value  = state.selector
            _groupSize.value = state.groupSize
            _cardSide.value  = state.cardSide
        }
    }

    /**
     * saveSettingState
     * Serializes object to JSON, writes to disk.
     * Persist the full object
     */
    private fun saveSettingState(state: SettingState) {
        scope.launch {
            try {
                val jsonString = json.encodeToString(state)
                dataStore.edit { prefs ->
                    prefs[KEY_SETTINGS_STATE] = jsonString
                }
                Environment.logInfo("$TAG: Saved State: Topic=${state.topic}, Source=${state.source.sourceName}")
            } catch (e: Exception) {
                Environment.logError("$TAG: Save failed. ${e.message}")
            }
        }
    }

    // --- SETTERS (Taking Enums) ---
// --- SETTERS (Refactored to use SettingState) ---

    fun setLanguage(newVal: String) {
        _language.value = newVal
        saveCurrentStateSnapshot()
    }

    fun setTopic(newVal: String) {
        _topic.value = newVal
        saveCurrentStateSnapshot()
    }

    fun setSource(newVal: FlashcardSource) {
        _source.value = newVal
        saveCurrentStateSnapshot()
    }

    fun setSelector(newVal: SelectorType) {
        _selector.value = newVal
        saveCurrentStateSnapshot()
    }

    fun setGroupSize(newVal: Int) {
        _groupSize.value = newVal
        saveCurrentStateSnapshot()
    }

    fun setCardSide(newVal: CardSide) {
        _cardSide.value = newVal
        saveCurrentStateSnapshot()
    }

    // Helper to gather current values and save
    private fun saveCurrentStateSnapshot() {
        val newState = SettingState(
            language  = _language.value,
            topic     = _topic.value,
            source    = _source.value,
            selector  = _selector.value,
            groupSize = _groupSize.value,
            cardSide  = _cardSide.value
        )
        saveSettingState(newState)
    }


}