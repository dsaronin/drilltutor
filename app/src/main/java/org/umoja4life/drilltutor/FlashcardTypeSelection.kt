package org.umoja4life.drilltutor

/**
 * FlashcardTypeSelection
 * The "Factory" and Registry that holds the specific handlers for each source.
 */
object FlashcardTypeSelection {

    // --- Specialized Handlers ---
    private val vocabularyType = VocabularyType()
    private val oppositesType = OppositesType()
    private val dictionaryType = DictionaryType()

    // --- Standard Handlers (Reusable Class) ---
    private val phrasesType   = StandardFlashcardType(FlashcardSource.PHRASES)
    private val sentencesType = StandardFlashcardType(FlashcardSource.SENTENCES)
    private val dialogsType   = StandardFlashcardType(FlashcardSource.DIALOGS)
    private val readingsType  = StandardFlashcardType(FlashcardSource.READINGS)
    private val glossariesType = StandardFlashcardType(FlashcardSource.GLOSSARIES)

    // Fallback
    private val standardType = StandardFlashcardType(FlashcardSource.UNKNOWN)

    /**
     * selectCardType
     * Returns the singleton instance for the requested Source.
     */
    fun selectCardType(source: FlashcardSource): AbstractFlashcardType {
        return when (source) {
            FlashcardSource.VOCABULARY -> vocabularyType
            FlashcardSource.OPPOSITES  -> oppositesType
            FlashcardSource.DICTIONARY -> dictionaryType

            FlashcardSource.PHRASES    -> phrasesType
            FlashcardSource.SENTENCES  -> sentencesType
            FlashcardSource.DIALOGS    -> dialogsType
            FlashcardSource.READINGS   -> readingsType
            FlashcardSource.GLOSSARIES -> glossariesType

            FlashcardSource.UNKNOWN -> standardType
        }
    }
}