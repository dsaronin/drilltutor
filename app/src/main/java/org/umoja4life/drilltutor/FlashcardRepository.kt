package org.umoja4life.drilltutor

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- STATUS ENUM ---
enum class DataStatus {
    Idle,       // No data loaded yet
    Loading,    // Currently parsing JSON
    Ready       // Data loaded and available
}
@Singleton
class FlashcardRepository @Inject constructor(
    private val dataSource: FlashcardDataSource
) {
    private val TAG = "FlashcardRepo"

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

}