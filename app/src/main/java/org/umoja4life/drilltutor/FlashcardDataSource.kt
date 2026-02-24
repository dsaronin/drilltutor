package org.umoja4life.drilltutor

interface FlashcardDataSource {

    /**
     * Loads the raw data.
     * Returns a Map where Key = Topic Name ("quantifiers"), Value = Topic Data.
     */
    suspend fun loadFile(languageCode: String, sourceType: FlashcardSource): Map<String, TopicData>?
    suspend fun getAvailableLanguages(): List<String>

    /**
     * Loads raw text from a file.
     * Used for non-JSON data sources like TSV dictionaries.
     */
    suspend fun loadTextFile(languageCode: String, sourceType: FlashcardSource): String?
}