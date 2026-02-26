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
        var myLanguage = Environment.settings.awaitLanguageLoad()


        // Execute the validation pipeline
        dataSource = validateState(currentStorage.storageUri, myLanguage)

        // Fetch the guaranteed valid language (in case validation changed it)
        myLanguage = Environment.settings.validLanguage()

        loadFlashcardData(myLanguage)
    }

    /**
     * validateState
     * Orchestrates the validation pipeline via functional composition.
     */
    private suspend fun validateState(storageUri: String, savedLanguage: String): FlashcardDataSource {
        return validateLanguage(savedLanguage, validateSource(storageUri))
    }

    /**
     * validateSource
     * Validates the selected storage directory and returns the appropriate data source.
     */
    private suspend fun validateSource(storageUri: String): FlashcardDataSource {
        // If empty, return default internal assets immediately
        if (storageUri.isBlank()) {
            return AssetDataSource(appContext)
        }

        // If a custom URI exists, validate it via FileDataSource
        if (FileDataSource.isValidSource(appContext, storageUri)) {
            // Note: FileDataSource constructor will need to accept appContext and storageUri
            return FileDataSource(appContext, storageUri)
        }

        // Fallback: URI exists but is invalid/inaccessible
        Environment.storage.resetToDefaultWithError("Custom folder is missing or inaccessible.")
        return AssetDataSource(appContext)
    }

    /**
     * validateLanguage
     * Verifies language exists in the data source. Mutates SettingsState if necessary.
     */
    private suspend fun validateLanguage(savedLanguage: String, dataSource: FlashcardDataSource): FlashcardDataSource {
        // Delegate state check and mutation entirely to SettingsRepository
        Environment.settings.updateLanguage(
            isAvailableLanguage(savedLanguage, dataSource)
        )

        return dataSource
    }

    /**
     * isAvailableLanguage
     * Core logic for determining fallback languages if the requested one is missing.
     */
    private suspend fun isAvailableLanguage(lang: String, dataSource: FlashcardDataSource): String {
        val langList = dataSource.getAvailableLanguages()

        if (langList.isNullOrEmpty()) {
            Environment.logError("$TAG: Data source contains no valid languages. Reverting to default.")
            return SettingsRepository.DEFAULT_LANGUAGE
        }

        if (!langList.contains(lang)) {
            Environment.logWarn("$TAG: Requested language '$lang' not found. Falling back to '${langList[0]}'.")
            return langList[0]
        }

        return lang
    }


}