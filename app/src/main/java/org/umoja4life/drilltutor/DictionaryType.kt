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
class DictionaryType : AbstractFlashcardType() {

    override fun getSourceName(): FlashcardSource = FlashcardSource.DICTIONARY

    override fun processData(data: Map<String, TopicData>?): Map<String, TopicData> {
        // Placeholder: Dictionary logic (TXT parsing) is coming in a future phase.
        // For now, return empty to prevent crashes.
        return emptyMap()
    }
}