package org.umoja4life.drilltutor

enum class FlashcardSource(val sourceName: String) {
    VOCABULARY("Vocabulary"),
    SENTENCES("Sentences"),
    OPPOSITES("Opposites"),
    PHRASES("Phrases"),
    DIALOGS("Dialogs"),
    READINGS("Readings"),
    GLOSSARIES("Glossaries"),
    DICTIONARY("Dictionary"),
    UNKNOWN("Unknown");

    companion object {
        fun fromSourceName(sourceName: String): FlashcardSource = entries.find { it.sourceName.equals(sourceName, ignoreCase = true) } ?: UNKNOWN
    }
}
