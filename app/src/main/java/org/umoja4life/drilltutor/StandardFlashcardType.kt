package org.umoja4life.drilltutor

/**
 * StandardFlashcardType
 * A reusable handler for source types that follow the standard JSON structure
 * and do not require special parsing logic (e.g., Phrases, Sentences, Dialogs).
 */
class StandardFlashcardType(
    source: FlashcardSource,
    data: TopicData? = null
) : AbstractFlashcardType(source) { // Must pass source to parent

    init {
        // We must populate the parent's data field
        this.topicData = data
    }

    override fun createInstance(data: TopicData?): AbstractFlashcardType {
        // Now this 2-arg call is valid
        return StandardFlashcardType(source, data)
    }

    override fun getSourceName(): FlashcardSource {
        return source
    }
}