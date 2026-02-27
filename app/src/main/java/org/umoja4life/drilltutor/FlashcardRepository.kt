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

    /**
     * executeDataLoad
     * Universal pipeline for loading or reloading data.
     * Ensures UI is suspended, data sources are validated, and databases are repopulated cleanly.
     */
    suspend fun executeDataLoad(requestedUri: String, requestedLang: String) {
        // NOTIFY: Tell listeners we are busy (suspends UI)
        _dataState.value = DataStatus.Loading

        Environment.logInfo("$TAG: Executing data load for URI: [$requestedUri], Lang: $requestedLang")

        // Execute the validation pipeline
        dataSource = validateState(requestedUri, requestedLang)

        // Fetch the guaranteed valid language (in case validation triggered a fallback)
        val validLang = Environment.settings.validLanguage()

        Environment.logInfo("$TAG: Loading Master Data for $validLang...")

        FlashcardSource.entries.forEach { source ->
            if (source != FlashcardSource.UNKNOWN) {
                val handler = FlashcardTypeSelection.selectCardType(source)
                try {
                    handler.load(dataSource, validLang)
                } catch (e: Exception) {
                    Environment.logError("$TAG: Failed to load ${source.sourceName}: ${e.message}")
                }
            }
        }
        Environment.logInfo("$TAG: Load Complete.")

        // NOTIFY: Tell listeners data is fresh and ready to use
        _dataState.value = DataStatus.Ready

        // TOAST: Notify user of successful data load
        Environment.toastInfo("Loaded DrillTutor for: $validLang")
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
        logInfo("$TAG: Bootup data starting...")

        // Add these two lines to test the new Storage State:
        val currentStorage = Environment.storage.loadStorageState()
        logInfo("$TAG: Previous Storage URI: [${currentStorage.storageUri}]")

        // Suspend until DataStore is ready (approx 20-50ms)
        // Delegate to the universal pipeline
        executeDataLoad(
            currentStorage.storageUri,
            Environment.settings.awaitLanguageLoad()
        )
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

        // TOAST: Notify user of the failure and fallback
        Environment.toastWarn("Invalid: missing language or data files")

        // Fallback: URI exists but is invalid/inaccessible
        Environment.storage.resetToDefaultWithError("folder is invalid or inaccessible.")
        Environment.settings.resetToDefaults()
        Environment.playerState.resetToDefaults()
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