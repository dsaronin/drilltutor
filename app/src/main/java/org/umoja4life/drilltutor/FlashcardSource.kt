package org.umoja4life.drilltutor

enum class FlashcardSource(val sourceName: String) {
    VOCABULARY("Vocabulary"),
    VERBS("Verbs"),
    SENTENCES("Sentences"),
    OPPOSITES("Opposites"),
    PHRASES("Phrases"),
    DIALOGS("Dialogs"),
    READINGS("Readings"),
    GLOSSARIES("Glossaries"),
    DICTIONARY("Dictionary"),
    LESSONS("Lessons"),
    UNKNOWN("Unknown");

    companion object {
        fun fromSourceName(sourceName: String): FlashcardSource = entries.find { it.sourceName.equals(sourceName, ignoreCase = true) } ?: UNKNOWN

        fun getSourceList(): List<FlashcardSource> = entries.filter { it != LESSONS && it != UNKNOWN }

        // --- NEW: Sources valid for mining examples ---
        fun getMiningSources(): List<FlashcardSource> = entries.filter {
            it != VOCABULARY &&
                    it != VERBS &&
                    it != OPPOSITES &&
                    it != LESSONS &&
                    it != UNKNOWN
        }
    }
}
