package org.umoja4life.drilltutor

/**
 * FlashcardSource
 * Represents the category of the flashcard deck.
 */

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