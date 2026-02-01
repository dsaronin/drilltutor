package org.umoja4life.drilltutor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlashcardRepository(private val dataSource: FlashcardDataSource) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "FlashcardRepo"

    // Holds the raw data in memory
    var vocabularyData: Map<String, TopicData>? = null
        private set

    // sorted TOPICS
    var topicList: List<String> = emptyList()
        private set

    /**
     *  Test Harness.
     */
    fun loadMasterData(languageCode: String) {
        scope.launch {
            Environment.logInfo("$TAG: --- STARTING REPO SMOKE TEST ---")

            // 1. Transplanted: List Languages
            // (Note: This prints the "Discovered languages" log you wanted to see)
            dataSource.getAvailableLanguages()

            // 2. Transplanted: Load Vocabulary (Expect Success)
            Environment.logDebug("$TAG: Attempting to load: $languageCode/vocabulary.json")
            val dataMap = dataSource.loadFile(languageCode, FlashcardSource.VOCABULARY)

            if (dataMap != null) {
                vocabularyData = dataMap
                val totalCards = dataMap.values.sumOf { it.fcData.size }
                Environment.logInfo("$TAG: SUCCESS: Loaded $languageCode/vocabulary.json. Found ${dataMap.size} topics, $totalCards total cards.")

                topicList =buildTopics() //  buildTopics list
            } else {
                Environment.logWarn("$TAG: FAILURE. Could not load data for $languageCode")
            }

            // 3. Transplanted: Load Unknown (Expect Failure/Null)
            Environment.logDebug("$TAG: Attempting to load: $languageCode/dictionary.json")
            dataSource.loadFile(languageCode, FlashcardSource.DICTIONARY)

            Environment.logInfo("$TAG: --- REPO SMOKE TEST COMPLETE ---")
        }
    }

    /**
     * Build Topics
     * Extracts topics field fm loaded data, builds sorted array, returns it.
     */
    fun buildTopics(): List<String> {
        // Reads from the nullable vocabularyData
        val topics = vocabularyData?.keys?.sorted() ?: emptyList()
        Environment.logInfo("$TAG: buildTopics called. Returning ${topics.size} topics.")
        return topics
    }

    // 2. Accessor
    fun getTopics(): List<String> {
        return topicList
    }
}