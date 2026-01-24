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

// Extension property to create the DataStore file (settings.preferences_pb)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object Environment {

    // --- IDENTITY ---
    const val APP_NAME = "DrillTutor"
    val VERSION_NAME: String = BuildConfig.VERSION_NAME
    val VERSION_CODE: String = BuildConfig.DRILLTUTOR_VERSION_CODE
    val IS_DEBUG: Boolean = BuildConfig.DEBUG

    // --- LOGGING ---
    private const val LOG_TAG = APP_NAME

    // --- PERSISTENCE KEYS ---
    private val KEY_LANGUAGE = stringPreferencesKey("language")

    // --- STATE (The "Live" Variables) ---
    // We use StateFlow so the UI can observe changes instantly.
    // Default is "en".
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    // --- INFRASTRUCTURE ---
    // A scope for background tasks that lives as long as the app process
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dataStore: DataStore<Preferences>

    // --- INITIALIZATION ---
    fun init(context: Context) {
        dataStore = context.dataStore

        logInfo("Environment Initializing...")

        // Load saved settings immediately
        // We use 'launch' so it doesn't block the startup
        scope.launch {
            val preferences = dataStore.data.first()
            val savedLang = preferences[KEY_LANGUAGE] ?: "en"
            _language.value = savedLang
            logInfo("Loaded Language from disk: $savedLang")
        }

        logInfo("Version: $VERSION_NAME (Build $VERSION_CODE)")
    }

    // --- ACTIONS (Setters) ---

    fun setLanguage(newLang: String) {
        // 1. Update memory immediately (UI reacts instantly)
        _language.value = newLang

        // 2. Save to disk in background
        scope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_LANGUAGE] = newLang
            }
            logInfo("Persisted Language change: $newLang")
        }
    }

    // --- LOGGING UTILITIES ---
    fun logInfo(msg: String) = Log.i(LOG_TAG, msg)
    fun logDebug(msg: String) { if (IS_DEBUG) Log.d(LOG_TAG, "DEBUG: $msg") }
    fun logWarn(msg: String) = Log.w(LOG_TAG, "WARN: $msg")
    fun logError(msg: String) = Log.e(LOG_TAG, "ERROR: $msg")
}