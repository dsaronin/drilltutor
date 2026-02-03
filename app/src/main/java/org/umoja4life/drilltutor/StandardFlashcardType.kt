package org.umoja4life.drilltutor

/**
 * StandardFlashcardType
 * A reusable handler for source types that follow the standard JSON structure
 * and do not require special parsing logic (e.g., Phrases, Sentences, Dialogs).
 *
 * Usage: Instantiate with a specific Source ID.
 * val phrases = StandardFlashcardType(FlashcardSource.PHRASES)
 */
class StandardFlashcardType(
    private val mySource: FlashcardSource
) : AbstractFlashcardType() {

    // Identity: Returns the specific source ID assigned to this instance
    override fun getSourceId(): FlashcardSource {
        return mySource
    }

    // Logic: Standard pass-through.
    // Accepts the loaded JSON map as-is without modification.
    override fun processData(data: Map<String, TopicData>?): Map<String, TopicData> {
        if (data != null) {
            return data
        } else {
            Environment.logWarn("StandardType (${mySource.id}): No data found. Returning empty.")
            return emptyMap()
        }
    }
}