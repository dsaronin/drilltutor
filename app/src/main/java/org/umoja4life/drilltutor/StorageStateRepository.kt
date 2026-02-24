package org.umoja4life.drilltutor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Separate file for Storage State persistence
private val Context.storageDataStore: DataStore<Preferences> by preferencesDataStore(name = "storage_state")

/**
 * StorageState
 * Data object holding the system environment configuration for file access.
 * Serializable for simple JSON persistence.
 */
@Serializable
data class StorageState(
    var storageUri: String = "" // Empty string implies default internal assets
)

class StorageStateRepository(private val context: Context) {

    private val dataStore = context.storageDataStore
    private val KEY_STORAGE_STATE = stringPreferencesKey("storage_state_json")

    // Configure JSON to be lenient
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * loadStorageState
     * Fetches stored JSON, decodes to object.
     * Returns default instance if missing or error.
     */
    suspend fun loadStorageState(): StorageState {
        return try {
            val preferences = dataStore.data.first()
            val jsonString = preferences[KEY_STORAGE_STATE]

            if (jsonString.isNullOrEmpty()) {
                Environment.logWarn("StorageState: No saved state found. Using defaults.")
                StorageState() // Return Defaults
            } else {
                val storageState = json.decodeFromString<StorageState>(jsonString)
                Environment.logInfo("StorageState: LoadStorageState: URI=[${storageState.storageUri}]")
                storageState
            }
        } catch (e: Exception) {
            Environment.logError("StorageState: Load failed. ${e.message}")
            StorageState() // Fallback to defaults
        }
    }

    /**
     * saveStorageState
     * Serializes object to JSON, writes to disk.
     */
    suspend fun saveStorageState(state: StorageState) {
        Environment.logInfo("StorageState: SaveStorageState: URI=[${state.storageUri}]")

        try {
            val jsonString = json.encodeToString(state)
            dataStore.edit { prefs ->
                prefs[KEY_STORAGE_STATE] = jsonString
            }
        } catch (e: Exception) {
            Environment.logError("StorageState: Save failed. ${e.message}")
        }
    }
}