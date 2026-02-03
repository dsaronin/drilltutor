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

    // database -- The "Repo" (Internal Memory Store, replaces @@database)
    // Protected so subclasses can read it, but only this class writes to it via load()
    protected var database: Map<String, TopicData> = emptyMap()

    /**
     * The Template Method: Orchestrates the loading process.
     * 1. Asks DataSource to load the specific file.
     * 2. Delegates processing to the subclass.
     * 3. Stores the result in the internal database.
     */
    suspend fun load(dataSource: FlashcardDataSource, languageCode: String) {
        val sourceId = getSourceId()

        Environment.logDebug("AbstractType: Loading ${sourceId.id} for $languageCode...")

        // 1. Get raw data
        val rawData = dataSource.loadFile(languageCode, sourceId)

        // 2. & 3. Process and Store
        database = processData(rawData)

        Environment.logInfo("AbstractType: Loaded ${database.size} topics for ${sourceId.id}.")
    }

    /**
     * Accessor for the data (The "get_item" equivalent)
     */
    fun getData(): Map<String, TopicData> {
        return database
    }

    /**
     * Extracts topics (The "sorted_keys" equivalent)
     */
    fun getTopics(): List<String> {
        return database.keys.sorted()
    }

    // --- CONTRACTS (Subclasses MUST implement these) ---

    // Identity: Who am I? (Vocabulary, Dictionary, etc.)
    abstract fun getSourceId(): FlashcardSource

    // Logic: How do I organize the raw data?
    abstract fun processData(data: Map<String, TopicData>?): Map<String, TopicData>
}