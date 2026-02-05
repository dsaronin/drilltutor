package org.umoja4life.drilltutor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Separate file for State persistence
private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_state")

/**
 * PlayerState
 * Data object passed to FlashManager.
 * Serializable for simple JSON persistence.
 */
@Serializable
data class PlayerState(
    val topicKey: String = "",
    val curPtr: Int = -1,     // -1 indicates fresh/reset state
    val groupDex: Int = 0,
    val showRear: Boolean = true
)

class PlayerStateRepository(private val context: Context) {

    private val dataStore = context.playerDataStore
    private val KEY_PLAYER_STATE = stringPreferencesKey("player_state_json")

    // Configure JSON to be lenient if needed
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * loadPlayerState
     * Fetches stored JSON, decodes to object.
     * Returns default instance if missing or error.
     */
    suspend fun loadPlayerState(): PlayerState {
        return try {
            val preferences = dataStore.data.first()
            val jsonString = preferences[KEY_PLAYER_STATE]

            if (jsonString.isNullOrEmpty()) {
                PlayerState() // Return Defaults
            } else {
                json.decodeFromString<PlayerState>(jsonString)
            }
        } catch (e: Exception) {
            Environment.logError("PlayerStateRepo: Load failed. ${e.message}")
            PlayerState() // Fallback to defaults
        }
    }

    /**
     * savePlayerState
     * Serializes object to JSON, writes to disk.
     */
    suspend fun savePlayerState(state: PlayerState) {
        try {
            val jsonString = json.encodeToString(state)
            dataStore.edit { prefs ->
                prefs[KEY_PLAYER_STATE] = jsonString
            }
        } catch (e: Exception) {
            Environment.logError("PlayerStateRepo: Save failed. ${e.message}")
        }
    }
}