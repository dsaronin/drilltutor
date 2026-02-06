package org.umoja4life.drilltutor

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepository @Inject constructor(
    private val dataSource: FlashcardDataSource
) {
    private val TAG = "FlashcardRepo"

    suspend fun loadFlashcardData(languageCode: String) {
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
    }
    // --- Accessors (Delegates to the Factory) ---

    fun getTopics(source: FlashcardSource): List<String> {
        val handler = FlashcardTypeSelection.selectCardType(source)
        return handler.getTopics()
    }

}