package org.umoja4life.drilltutor

/**
 * AbstractFlashcardType
 * Defines the behavior contract for different flashcard types.
 *
 * Now includes DEFAULT implementations for standard behavior.
 * Sub-classes only need to override if they deviate from the raw data.
 */
interface AbstractFlashcardType {

    // --- DISPLAY BEHAVIOR (Default: Return Raw Data) ---

    fun getFrontText(data: FlashcardData): String {
        return data.rawFront
    }

    fun getBackText(data: FlashcardData): String {
        return data.rawBack
    }

    fun getNotesText(data: FlashcardData): String {
        return data.notes
    }

    // --- SEARCH/MINING BEHAVIOR (Default: Search Front) ---

    fun getSearchableText(data: FlashcardData): String {
        return data.rawFront
    }

    fun supportsMining(): Boolean = true

    // --- DEFAULTS ---

    fun getDefaultCard(): FlashcardData {
        return FlashcardData(
            id = "default",
            source = SourceType.UNKNOWN,
            topic = "default",
            front = "boş",
            back = "tupu",
            notes = "No Data"
        )
    }
}