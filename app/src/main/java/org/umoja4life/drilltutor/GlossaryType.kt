package org.umoja4life.drilltutor

/**
 * GlossaryType
 * Handler for Glossary types that follow the standard JSON structure
 * and do not require special parsing logic (e.g., Phrases, Sentences, Dialogs).
 */
class GlossaryType(
    source: FlashcardSource,
    data: TopicData? = null
) : AbstractFlashcardType(source) { // Must pass source to parent

    init {
        // We must populate the parent's data field
        this.topicData = data
    }

    override fun createInstance(data: TopicData?): AbstractFlashcardType {
        // Now this 2-arg call is valid
        return GlossaryType(source, data)
    }

    override fun getSourceName(): FlashcardSource {
        return source
    }

    fun findGlossary(key: String): TopicData? {
        // Get the Wrapper (AbstractFlashcardType)
        val instance = database[key] ?: return null

        // Return the Raw Data (TopicData)
        return instance.topicData
    }
}