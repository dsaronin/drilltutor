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
    fun selectCardType(source: SourceType): AbstractFlashcardType {
        return when (source) {
            SourceType.OPPOSITES -> oppositesType
            SourceType.DICTIONARY -> dictionaryType

            // All these share the Standard Behavior
            SourceType.VOCABULARY,
            SourceType.SENTENCES,
            SourceType.PHRASES,
            SourceType.DIALOGS,
            SourceType.READINGS,
            SourceType.GLOSSARIES,
            SourceType.UNKNOWN -> standardType

            // Default fallback
            else -> standardType
        }
    }
}