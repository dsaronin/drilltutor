package org.umoja4life.drilltutor

abstract class AbstractFlashcardType {

    /**
     * Converts a raw list of strings (from JSON) into a structured FlashcardData object.
     * Default implementation assumes [0] = Front, [1] = Back.
     */
    open fun createFlashcard(
        rawData: List<String>,
        topic: String,
        source: FlashcardSource // FIX: Updated Enum
    ): FlashcardData {

        // FIX: Safe extraction of data (fixing the "Unresolved reference" errors)
        val rawFront = rawData.getOrElse(0) { "" }
        val rawBack  = rawData.getOrElse(1) { "" }

        return FlashcardData(
            id = generateId(topic, rawFront),
            source = source,
            topic = topic,
            front = rawFront,
            back = rawBack,
            notes = ""
        )
    }

    // Helper to generate a consistent ID
    protected fun generateId(topic: String, front: String): String {
        // Simple sanitization to create a safe ID
        return "${topic}_${front.take(10)}".replace(" ", "_").lowercase()
    }
}