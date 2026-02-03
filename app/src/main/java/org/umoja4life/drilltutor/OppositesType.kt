package org.umoja4life.drilltutor

/**
 * OppositesType
 * Handler for Opposites flashcards.
 */
class OppositesType : AbstractFlashcardType() {

    override fun getSourceId(): FlashcardSource = FlashcardSource.OPPOSITES

    override fun processData(data: Map<String, TopicData>?): Map<String, TopicData> {
        // Standard JSON handling (similar to Vocabulary)
        return data ?: emptyMap()
    }
}