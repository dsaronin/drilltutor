package org.umoja4life.drilltutor

/**
 * DictionaryType
 * Placeholder for the Dictionary functionality.
 *
 * Legacy Behavior (dictionary.rb):
 * - Reads 'dictionary.txt' (TSV format).
 * - Front: Key (Headword).
 * - Back: Concatenation of all definitions.
 *
 * Current Android Behavior:
 * - Uses Standard behavior (returning raw data if available).
 * - Serves as a stub for future expansion.
 */
class DictionaryType(
    source: FlashcardSource,
    data: TopicData? = null
) : AbstractFlashcardType(source) {

    // *************************************************************
    // *************************************************************
    companion object {
        const val FIELD_DELIMITER = "\t"
        const val ENTRY = 0
        const val DEF = 1
        const val EX = 2

        // Matches the ruby regex: \s*{.+}
        val REGEX_STRIP = Regex("\\s*\\{.+\\}")

        // Equivalent to Ruby's @@data
        // Maps: Headword -> List of [Definition, Example] lists
        val dictionaryData = mutableMapOf<String, MutableList<List<String>>>()

        fun mineExamples(key: String): List<String> {
            val defs = dictionaryData[key] ?: return emptyList()

            // Map to the example string (index 1) and filter out blanks/nulls
            return defs.mapNotNull { it.getOrNull(1) }.filter { it.isNotBlank() }
        }

    }
    // *************************************************************
    // *************************************************************

    init {
        this.topicData = data
    }

    override fun getSourceName(): FlashcardSource = source

    // Equivalent to @fc_keys = @@data.keys
    // Equivalent to @fc_keys = @@data.keys
    private val fcKeys: List<String>
        get() = dictionaryData.keys.toList()



    /**
     * createInstance
     * Implementation for the Factory/Worker pattern.
     * Returns a new DictionaryType bound to the specific topic data.
     */
    override fun createInstance(data: TopicData?): AbstractFlashcardType {
        return DictionaryType(source, data)
    }

    override fun listSize(): Int = fcKeys.size


    /**
     * Override standard JSON loading to use the text file parser.
     */
    override suspend fun load(dataSource: FlashcardDataSource, languageCode: String) {
        Environment.logDebug("DictionaryType: Loading custom TSV for $languageCode...")

        // Fetch raw text via the new pipeline
        val rawText = dataSource.loadTextFile(languageCode, source)

        if (rawText.isNullOrBlank()) {
            Environment.logWarn("DictionaryType: No text data found for ${source.sourceName}")
            return
        }

        // Populate the class-level @@data equivalent
        dictionaryData.clear()
        dictionaryData.putAll(processData(rawText))  // Parse text and populate the inherited database

        // Populate the inherited @@database with a single worker instance
        // Equivalent to: @@database[:dictionary] = Dictionary.new
        val instance = createInstance(null)
        instance.myTopic = "dictionary"

        database.clear()
        database["dictionary"] = instance

        val totalDefinitions = dictionaryData.values.sumOf { it.size }
        Environment.logInfo("DictionaryType: Loaded dictionary database. Found ${dictionaryData.size} headwords, $totalDefinitions total definitions.")
    }

    private fun processData(rawText: String): MutableMap<String, MutableList<List<String>>> {
        // This is the equivalent of Ruby's @@data
        val dataHash = mutableMapOf<String, MutableList<List<String>>>()

        rawText.lines().forEach { line ->
            if (line.isBlank()) return@forEach

            val fields = line.split(FIELD_DELIMITER)
            var key = fields.getOrNull(ENTRY)

            if (key != null) {
                // Equivalent to: key.gsub!( /\s*{.+}/, "")
                key = key.replace(REGEX_STRIP, "")

                // Equivalent to: unless key.nil? (and checking for empty)
                if (key.isNotEmpty()) {
                    // Equivalent to: @@data[key] ||= []
                    val keyList = dataHash.getOrPut(key) { mutableListOf() }

                    // Equivalent to: list = fields[DEF..EX]
                    val list = if (fields.size > DEF) {
                        fields.subList(DEF, minOf(fields.size, EX + 1))
                    } else {
                        emptyList()
                    }

                    // Equivalent to: @@data[key] <<= ( list.nil? || list.empty?  ?  [""]  :  list )
                    keyList.add(if (list.isEmpty()) listOf("") else list)
                }
            }
        }

        return dataHash
    }

    override fun getDataAtIndex(index: Int, side: String): FlashcardData {
        if (fcKeys.isEmpty()) return Environment.emptyFlashcardData[0]

        val clampedIndex = clampIndex(index, fcKeys.size)
        val key = fcKeys[clampedIndex]

        // Mash together the definitions (index 0 of each sublist)
        val defs = dictionaryData[key] ?: emptyList()
        val back = defs.joinToString("; ") { it.getOrElse(0) { "" } }

        val card = FlashcardData(front = key, back = back)
        return sideConversion(card, side)
    }

    override fun mineExamples(key: String): List<String> {
        return emptyList()
    }

}
