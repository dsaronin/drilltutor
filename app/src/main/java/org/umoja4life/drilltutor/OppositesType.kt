package org.umoja4life.drilltutor

/**
 * OppositesType
 * Handler for Opposites flashcards.
 *
 * Inherits display behavior (Raw Front/Back) from interface.
 * Overrides only the Mining behavior.
 */
class OppositesType : AbstractFlashcardType {

    override fun getSearchableText(data: FlashcardData): String {
        return "" // Not searchable
    }

    override fun supportsMining(): Boolean {
        return false // Legacy behavior: Disabled
    }
}