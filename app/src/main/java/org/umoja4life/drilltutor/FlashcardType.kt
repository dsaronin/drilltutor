package org.umoja4life.drilltutor

/**
 * FlashcardType
 * The "Standard" implementation of the behavior contract.
 * Used for Vocabulary, Sentences, Phrases, etc.
 *
 * Inherits all default behavior from AbstractFlashcardType:
 * - Front/Back/Notes return raw data.
 * - Mining is enabled on the Front text. */

class FlashcardType : AbstractFlashcardType() { }
