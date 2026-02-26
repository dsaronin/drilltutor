package org.umoja4life.drilltutor

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.umoja4life.drilltutor.Environment.logInfo
import javax.inject.Singleton

// --- STATUS ENUM ---
enum class DataStatus {
    Idle,       // No data loaded yet
    Loading,    // Currently parsing JSON
    Ready       // Data loaded and available
}
@Singleton
class FlashcardRepository(
    private val appContext: Context
) {
    private val TAG = "FlashcardRepo"
    private lateinit var dataSource: FlashcardDataSource

    // --- State Monitoring ---
    // Start as Idle. DrillViewModel observes this to know when to rebuild.
    private val _dataState = MutableStateFlow<DataStatus>(DataStatus.Idle)
    val dataState: StateFlow<DataStatus> = _dataState.asStateFlow()

    suspend fun loadFlashcardData(languageCode: String) {
// NOTIFY: Tell listeners we are busy
        _dataState.value = DataStatus.Loading

        Environment.logInfo("$TAG: Loading Master Data for $languageCode...")

        FlashcardSource.entries.forEach { source ->
            if (source != FlashcardSource.UNKNOWN) {
                // Get the Singleton Handler from the Factory
                val handler = FlashcardTypeSelection.selectCardType(source)

                try {
                    // Load data into that Handler instance
                    // Now this effectively waits because the function is suspend
                    handler.load(dataSource, languageCode)
                } catch (e: Exception) {
                    Environment.logError("$TAG: Failed to load ${source.sourceName}: ${e.message}")
                }
            }
        }
        Environment.logInfo("$TAG: Load Complete.")

        // NOTIFY: Tell listeners data is fresh and ready to use
        _dataState.value = DataStatus.Ready
    }
    // --- Accessors (Delegates to the Factory) ---

    fun getTopics(source: FlashcardSource): List<String> {
        val handler = FlashcardTypeSelection.selectCardType(source)
        return handler.getTopics()
    }

    suspend fun getAvailableLanguages(): List<String> {
        return dataSource.getAvailableLanguages()
    }

    /**
     * bootup
     * Sequential startup logic:
     * 1. Waists for Settings to be read from disk.
     * 2. Loads the correct Flashcard data based on the saved language.
     */
     suspend fun bootup() {
        logInfo("$TAG: Waiting for saved settings...")

        // Add these two lines to test the new Storage State:
        val currentStorage = Environment.storage.loadStorageState()
        logInfo("$TAG: Previous Storage URI: [${currentStorage.storageUri}]")

        // Suspend until DataStore is ready (approx 20-50ms)
        val savedLanguage = Environment.settings.awaitLanguageLoad()

        // TEMPORARY: Hardcoded structural test
        dataSource = AssetDataSource(appContext)

        logInfo("$TAG: Settings loaded ($savedLanguage). Triggering data load...")
        loadFlashcardData(savedLanguage)
    }


}