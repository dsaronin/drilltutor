package org.umoja4life.drilltutor

/**
 * StandardFlashcardType
 * A reusable handler for source types that follow the standard JSON structure
 * and do not require special parsing logic (e.g., Phrases, Sentences, Dialogs).
 */
class StandardFlashcardType(private val type: FlashcardSource) : AbstractFlashcardType() {

    override fun getSourceName(): FlashcardSource {
        return type
    }
}