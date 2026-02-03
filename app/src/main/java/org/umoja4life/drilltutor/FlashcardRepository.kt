package org.umoja4life.drilltutor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlashcardRepository(private val dataSource: FlashcardDataSource) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "FlashcardRepo"

    /**
     * loadMasterData
     * Loops through ALL defined sources and triggers their specific load logic.
     */
    fun loadMasterData(languageCode: String) {
        scope.launch {
            Environment.logInfo("$TAG: Loading Master Data for $languageCode...")

            // Iterate over all valid sources
            FlashcardSource.entries.forEach { source ->
                if (source != FlashcardSource.UNKNOWN) {
                    // 1. Get the handler (VocabularyType, PhrasesType, etc.)
                    val handler = FlashcardTypeSelection.selectCardType(source)

                    // 2. Trigger the Template Method load
                    handler.load(dataSource, languageCode)
                }
            }

            Environment.logInfo("$TAG: Load Complete.")
        }
    }

    /**
     * getTopics
     * Returns the topics for the requested source.
     * (Defaults to VOCABULARY if no source is specified, to keep legacy calls working)
     */
    fun getTopics(source: FlashcardSource = FlashcardSource.VOCABULARY): List<String> {
        val handler = FlashcardTypeSelection.selectCardType(source)
        return handler.getTopics()
    }

    /**
     * getFlashcardData
     * Helper to access the raw data map for a specific source.
     */
    fun getFlashcardData(source: FlashcardSource): Map<String, TopicData> {
        val handler = FlashcardTypeSelection.selectCardType(source)
        return handler.getData()
    }
}