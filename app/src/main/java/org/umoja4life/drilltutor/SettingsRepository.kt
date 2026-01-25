package org.umoja4life.drilltutor

import android.content.Context
import android.util.Log
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

// 1. Define the DataStore extension here (moved from Environment)
// This creates a file named "settings.preferences_pb"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val TAG = "SettingsRepo"

    // --- INFRASTRUCTURE ---
    // Scope for background saves
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore: DataStore<Preferences> = context.dataStore

    // --- KEYS ---
    private val KEY_LANGUAGE = stringPreferencesKey("language")

    // --- STATE FLOWS (Publicly observable data) ---
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        // Load initial state immediately upon creation
        loadSettings()
    }

    private fun loadSettings() {
        scope.launch {
            val prefs = dataStore.data.first()
            val savedLang = prefs[KEY_LANGUAGE] ?: "en"

            // Update memory
            _language.value = savedLang

            Environment.logInfo("$TAG: Loaded Language from disk: $savedLang")
        }
    }

    // --- SETTERS ---

    fun setLanguage(newLang: String) {
        _language.value = newLang
        scope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = newLang
            }
            Environment.logInfo("$TAG: Persisted Language: $newLang")
        }
    }
}