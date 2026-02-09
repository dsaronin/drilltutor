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
    companion object {
        const val DEFAULT_TOPIC = "default"
        val DEFAULT_SOURCE = FlashcardSource.VOCABULARY
        val DEFAULT_SELECTOR = SelectorType.ORDERED
        const val DEFAULT_SIZE = 5
        val DEFAULT_SIDE = CardSide.FRONT
        val VALID_GROUP_SIZES = listOf(5, 10, 15, 25, 50)
    }

    private val TAG = "SettingsRepo"

    // --- INFRASTRUCTURE ---
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    // --- PERSISTENCE KEYS ---
    private val KEY_SETTINGS_STATE = stringPreferencesKey("settings_state_json")

    // --- STATE FLOWS ---
    private val _settingState = MutableStateFlow(SettingState())
    val settingState: StateFlow<SettingState> = _settingState.asStateFlow()

    init {
        loadSettingState()
    }

    private fun loadSettingState() {
        scope.launch {
            val prefs = dataStore.data.first()
            val jsonString = prefs[KEY_SETTINGS_STATE]

            val state = if (jsonString.isNullOrEmpty()) {
                Environment.logInfo("$TAG: No saved state found. Using Defaults.")
                SettingState()
            } else {
                try {
                    json.decodeFromString<SettingState>(jsonString)
                } catch (e: Exception) {
                    Environment.logError("$TAG: Load failed. ${e.message}")
                    SettingState()
                }
            }

            Environment.logInfo("$TAG: Loaded State: Topic=${state.topic}, Source=${state.source.sourceName}")
            _settingState.value = state
        }
    }

    // --- UNIFIED UPDATER ---
    fun updateSettings(newState: SettingState) {
        _settingState.value = newState // Instant UI update
        saveSettingState(newState)     // Async persistence
    }

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
}