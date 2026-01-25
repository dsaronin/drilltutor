package org.umoja4life.drilltutor

import androidx.annotation.StringRes

/**
 * FlashcardSource
 * Represents the category of the flashcard deck.
 */
enum class FlashcardSource(val id: String) {
    VOCABULARY("Vocabulary"),
    OPPOSITES("Opposites"),
    SENTENCES("Sentences"),
    PHRASES("Phrases"),
    DIALOGS("Dialogs"),
    READINGS("Readings"),
    GLOSSARIES("Glossaries"),
    DICTIONARY("Dictionary");

    // Helper to find by ID (safe lookup)
    companion object {
        fun fromId(id: String): FlashcardSource = entries.find { it.id == id } ?: VOCABULARY
    }
}

/**
 * SelectorType
 * Determines if cards are shown in order or shuffled.
 */
enum class SelectorType(val id: String) {
    ORDERED("ordered"),
    SHUFFLED("shuffled");

    companion object {
        fun fromId(id: String): SelectorType = entries.find { it.id == id } ?: ORDERED
    }
}

/**
 * CardSide
 * Determines which side of the card is shown first.
 */
enum class CardSide(val id: String) {
    FRONT("front"),
    BACK("back"),
    SHUFFLE("shuffle");

    companion object {
        fun fromId(id: String): CardSide = entries.find { it.id == id } ?: FRONT
    }
}