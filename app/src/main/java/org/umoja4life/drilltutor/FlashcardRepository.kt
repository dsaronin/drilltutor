package org.umoja4life.drilltutor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepository @Inject constructor(
    private val dataSource: FlashcardDataSource
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "FlashcardRepo"

    fun loadFlashcardData(languageCode: String) {
        scope.launch {
            Environment.logInfo("$TAG: Loading Master Data for $languageCode...")

            FlashcardSource.entries.forEach { source ->
                if (source != FlashcardSource.UNKNOWN) {
                    // Get the Singleton Handler from the Factory
                    val handler = FlashcardTypeSelection.selectCardType(source)

                    try {
                        // Load data into that Handler instance
                        handler.load(dataSource, languageCode, source)
                    } catch (e: Exception) {
                        Environment.logError("$TAG: Failed to load ${source.sourceName}: ${e.message}")
                    }
                }
            }
            Environment.logInfo("$TAG: Load Complete.")
        }
    }

    // --- Accessors (Delegates to the Factory) ---

    fun getTopics(source: FlashcardSource): List<String> {
        val handler = FlashcardTypeSelection.selectCardType(source)
        return handler.getTopics()
    }

}