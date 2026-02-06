package org.umoja4life.drilltutor

/**
 * DictionaryType
 * Placeholder for the Dictionary functionality.
 *
 * Legacy Behavior (dictionary.rb):
 * - Reads 'dictionary.txt' (TSV format).
 * - Front: Key (Headword).
 * - Back: Concatenation of all definitions.
 *
 * Current Android Behavior:
 * - Uses Standard behavior (returning raw data if available).
 * - Serves as a stub for future expansion.
 */
class DictionaryType(
    source: FlashcardSource,
    data: TopicData? = null
) : AbstractFlashcardType(source) {

    init {
        this.topicData = data
    }

    override fun getSourceName(): FlashcardSource = source

    /**
     * createInstance
     * Implementation for the Factory/Worker pattern.
     * Returns a new DictionaryType bound to the specific topic data.
     */
    override fun createInstance(data: TopicData?): AbstractFlashcardType {
        return DictionaryType(source, data)
    }

    fun processData(data: Map<String, TopicData>?): Map<String, TopicData> {
        // Placeholder: Dictionary logic (TXT parsing) is coming in a future phase.
        // For now, return empty to prevent crashes.
        return emptyMap()
    }
}
