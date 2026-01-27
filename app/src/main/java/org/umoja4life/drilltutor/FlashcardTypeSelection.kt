package org.umoja4life.drilltutor

/**
 * FlashcardTypeSelection
 * The "Factory" that decides which Behavior Type to use based on the SourceType.
 */
object FlashcardTypeSelection {

    // Instantiate our types once (since they are stateless)
    private val standardType = FlashcardType()
    private val oppositesType = OppositesType()
    private val dictionaryType = DictionaryType()

    /**
     * selectCardType
     * Returns the correct Behavior Type for the given Data Source.
     */
    fun selectCardType(source: FlashcardSource): AbstractFlashcardType {
        return when (source) {
            FlashcardSource.OPPOSITES -> oppositesType
            FlashcardSource.DICTIONARY -> dictionaryType

            FlashcardSource.VOCABULARY,
            FlashcardSource.SENTENCES,
            FlashcardSource.PHRASES,
            FlashcardSource.DIALOGS,
            FlashcardSource.READINGS,
            FlashcardSource.GLOSSARIES,
            FlashcardSource.UNKNOWN -> standardType

            else -> standardType
        }
    }
}