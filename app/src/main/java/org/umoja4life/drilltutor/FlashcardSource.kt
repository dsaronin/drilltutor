package org.umoja4life.drilltutor

enum class FlashcardSource(val id: String) {
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
        fun fromId(id: String): FlashcardSource = entries.find { it.id.equals(id, ignoreCase = true) } ?: UNKNOWN
    }
}