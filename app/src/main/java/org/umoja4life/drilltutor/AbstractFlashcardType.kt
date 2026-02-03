package org.umoja4life.drilltutor

abstract class AbstractFlashcardType {

    /**
     * Converts a raw list of strings (from JSON) into a structured FlashcardData object.
     * Axiom #1: A flashcard is strictly (front, back).
     */
    open fun createFlashcard(rawData: List<String>): FlashcardData {
        val rawFront = rawData.getOrElse(0) { "" }
        val rawBack  = rawData.getOrElse(1) { "" }

        return FlashcardData(
            front = rawFront,
            back = rawBack
        )
    }

    // database -- The "Repo" (Internal Memory Store)
    // Stores the Rich TopicData (Metadata + Cards)
    protected var database: Map<String, TopicData> = emptyMap()

    /**
     * The Template Method: Orchestrates the loading process.
     * Updated: Parameter renamed to 'source' (The Enum itself).
     */
    suspend fun load(dataSource: FlashcardDataSource, languageCode: String, source: FlashcardSource) {

        Environment.logDebug("AbstractType: Loading ${source.sourceName} for $languageCode...")

        // 1. Get raw data
        val rawData = dataSource.loadFile(languageCode, source)

        // 2. & 3. Process and Store
        database = processData(rawData)

        Environment.logInfo("AbstractType: Loaded ${database.size} topics for ${source.sourceName}.")
    }

    /**
     * Accessor for the data
     */
    fun getData(): Map<String, TopicData> {
        return database
    }

    /**
     * Extracts topics
     */
    fun getTopics(): List<String> {
        return database.keys.sorted()
    }

    // --- LOGIC ---

    /**
     * Logic: How do I organize the raw data?
     * Default: Passthrough. Subclasses can override.
     */
    open fun processData(data: Map<String, TopicData>?): Map<String, TopicData> {
        return data ?: emptyMap()
    }

    // Identity: Who am I? (Vocabulary, Dictionary, etc.)

    abstract fun getSourceName(): FlashcardSource
}
