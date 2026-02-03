package org.umoja4life.drilltutor

class VocabularyType : AbstractFlashcardType() {

    // Identity: I am Vocabulary
    override fun getSourceName(): FlashcardSource = FlashcardSource.VOCABULARY

    // Logic: I accept the standard JSON structure as-is.
    override fun processData(data: Map<String, TopicData>?): Map<String, TopicData> {
        if (data != null) {
            return data
        } else {
            Environment.logWarn("VocabularyType: No data found (or load failed). Returning empty.")
            return emptyMap()
        }
    }
}