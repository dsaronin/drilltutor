package org.umoja4life.drilltutor

import java.util.concurrent.ConcurrentHashMap

/**
 * FlashcardTypeSelection
 * The "Factory" and Registry that holds the specific handlers for each source.
 */
object FlashcardTypeSelection {

    // Cache to hold the singleton instances of our handlers.
    // This replaces the long list of "private val xyzType = ..."
    private val handlerCache = ConcurrentHashMap<FlashcardSource, AbstractFlashcardType>()

    /**
     * selectCardType
     * Returns the singleton instance for the requested Source.
     */
    fun selectCardType(source: FlashcardSource): AbstractFlashcardType {
        return handlerCache.getOrPut(source) {
            // This block runs ONLY the first time a source is requested.
            when (source) {
                FlashcardSource.DICTIONARY -> DictionaryType()

                // STANDARD CASES: Everything else uses the Standard handler.
                else -> StandardFlashcardType(source)
            }
        }
    }
}