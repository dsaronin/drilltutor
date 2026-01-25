package org.umoja4life.drilltutor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val TAG = "SettingsRepo"

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

    // --- INFRASTRUCTURE ---
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore: DataStore<Preferences> = context.dataStore

    // --- PERSISTENCE KEYS ---
    private val KEY_LANGUAGE = stringPreferencesKey("language")
    private val KEY_TOPIC    = stringPreferencesKey("topic")
    private val KEY_SOURCE   = stringPreferencesKey("source")
    private val KEY_SELECTOR = stringPreferencesKey("selector")
    private val KEY_SIZE     = intPreferencesKey("size")
    private val KEY_SIDE     = stringPreferencesKey("side")

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
        loadSettings()
    }

    private fun loadSettings() {
        scope.launch {
            val prefs = dataStore.data.first()

            // 1. Load Raw Values
            val rawLang   = prefs[KEY_LANGUAGE] ?: "en"
            val rawTopic  = prefs[KEY_TOPIC]    ?: DEFAULT_TOPIC
            val rawSource = prefs[KEY_SOURCE]   ?: DEFAULT_SOURCE.id
            val rawSelect = prefs[KEY_SELECTOR] ?: DEFAULT_SELECTOR.id
            val rawSize   = prefs[KEY_SIZE]     ?: DEFAULT_SIZE
            val rawSide   = prefs[KEY_SIDE]     ?: DEFAULT_SIDE.id

            // 2. Convert to Types (Safety Check)
            _language.value  = rawLang
            _topic.value     = rawTopic
            _source.value    = FlashcardSource.fromId(rawSource)
            _selector.value  = SelectorType.fromId(rawSelect)
            _groupSize.value = rawSize
            _cardSide.value  = CardSide.fromId(rawSide)

            Environment.logInfo("$TAG: Loaded Settings: Lang=$rawLang, Source=${_source.value.id}")
        }
    }

    // --- SETTERS (Taking Enums) ---

    fun setLanguage(newVal: String) = saveString(KEY_LANGUAGE, _language, newVal)
    fun setTopic(newVal: String)    = saveString(KEY_TOPIC, _topic, newVal)

    fun setSource(newVal: FlashcardSource) {
        _source.value = newVal
        saveInternal(KEY_SOURCE, newVal.id)
    }

    fun setSelector(newVal: SelectorType) {
        _selector.value = newVal
        saveInternal(KEY_SELECTOR, newVal.id)
    }

    fun setCardSide(newVal: CardSide) {
        _cardSide.value = newVal
        saveInternal(KEY_SIDE, newVal.id)
    }

    fun setGroupSize(newVal: Int) {
        _groupSize.value = newVal
        scope.launch {
            dataStore.edit { it[KEY_SIZE] = newVal }
            Environment.logInfo("$TAG: Saved Size: $newVal")
        }
    }

    // --- HELPERS ---

    // Helper for simple strings
    private fun saveString(key: Preferences.Key<String>, flow: MutableStateFlow<String>, newVal: String) {
        flow.value = newVal
        saveInternal(key, newVal)
    }

    // Bottom-level saver
    private fun saveInternal(key: Preferences.Key<String>, value: String) {
        scope.launch {
            dataStore.edit { it[key] = value }
            Environment.logInfo("$TAG: Saved ${key.name}: $value")
        }
    }
}