package org.umoja4life.drilltutor

import kotlin.random.Random

/**
 * AbstractFlashcardType
 *
 * Modeled after the Ruby 'Sources' mixin.
 * Acts as the Factory (Class) and the Worker (Instance).
 */
abstract class AbstractFlashcardType(
    val source: FlashcardSource
) {

    companion object {
        val EMPTY_DATA = listOf(FlashcardData("boş", "tupu"))
    }

    // IDENTITY
    abstract fun getSourceName(): FlashcardSource

    // ------------------------------------------------------------
    // INSTANCE STATE (The Worker)
    // ------------------------------------------------------------

    // The specific data for this instance (Ruby: @fc_data via YAML load)
    protected var topicData: TopicData? = null

    // Ruby: attr_accessor :my_topic
    var myTopic: String = ""

    // ------------------------------------------------------------
    // DATABASE STATE (The Factory)
    // ------------------------------------------------------------

    // Ruby: @@database
    // Maps: Topic Key -> Fully Instantiated Worker Object
    // The key is the String from the JSON (e.g., "adverbs"), the Value is the Object (e.g., Vocabulary instance)
    protected var database: MutableMap<String, AbstractFlashcardType> = mutableMapOf()

    // ------------------------------------------------------------
    // CLASS METHODS (Factory Logic)
    // ------------------------------------------------------------

    /**
     * load
     * Reads raw JSON data via DataSource, converts it to Instances, and fills the Database.
     */
    suspend fun load(dataSource: FlashcardDataSource, languageCode: String) {

        Environment.logDebug("AbstractType: Loading ${source.sourceName} for $languageCode...")

        // 1. Get raw data (Map<String, TopicData>) from AssetDataSource
        val rawData = dataSource.loadFile(languageCode, source)

        if (rawData == null) {
            Environment.logWarn("AbstractType: No data found for ${source.sourceName}")
            return
        }

        // 2. Process and Store
        // Transform the raw TopicData structs into active Worker Objects
        database = processData(rawData)

        Environment.logInfo("AbstractType: Loaded ${database.size} topics for ${source.sourceName}.")
    }

    /**
     * processData
     * Transforms raw Map<String, TopicData> -> MutableMap<String, AbstractFlashcardType>
     */
    private fun processData(rawData: Map<String, TopicData>): MutableMap<String, AbstractFlashcardType> {
        val processedDb = mutableMapOf<String, AbstractFlashcardType>()

        for ((key, data) in rawData) {
            // Ruby: !ruby/object:Vocabulary ...
            // We manually instantiate the wrapper here using the Factory method.
            val instance = createInstance(data)
            instance.myTopic = key
            processedDb[key] = instance
        }
        return processedDb
    }

    /**
     * findOrNew
     * The Factory Accessor.
     * Returns the Pre-Loaded Object from the database.
     */
    fun findOrNew(key: String, entry: String? = null): AbstractFlashcardType {
        // Ruby: use_key = ( entry.nil? ? key : entry )
        val useKey = entry ?: key

        // Ruby: obj = db[use_key]
        var obj = database[useKey]

        // Ruby: if obj.nil? ... (Implicit fallback or new allocation)
        if (obj == null) {
            // Ruby: allocate; obj.send(:initialize, key)
            // Create Empty Instance
            obj = createInstance(null)
            obj.myTopic = key
        } else {
            // Ruby: obj.my_topic = use_key
            obj.myTopic = useKey
        }

        return obj
    }

    // ------------------------------------------------------------
    // INSTANCE METHODS (Worker Logic)
    // ------------------------------------------------------------

    /**
     * fcData
     * Accessor for the card list.
     */
    fun fcData(): List<FlashcardData> {
        return topicData?.fcData ?: EMPTY_DATA
    }

    /**
     * listSize
     */
    fun listSize(): Int = fcData().size

    /**
     * getDataAtIndex
     * Returns the formatted tuple (Front/Back/Shuffled).
     */
    fun getDataAtIndex(index: Int, side: String = "front"): FlashcardData {
        val list = fcData()
        if (list.isEmpty()) return EMPTY_DATA[0]

        val clamped = clampIndex(index, list.size)
        val card = list[clamped]

        return sideConversion(card, side)
    }

    /**
     * clampIndex
     */
    private fun clampIndex(index: Int, length: Int): Int {
        var idx = index
        if (idx < 0) idx = 0
        if (idx >= length) idx = length - 1
        return idx
    }

    /**
     * sideConversion
     * Handles Front/Back swapping or Shuffling.
     */
    private fun sideConversion(card: FlashcardData, side: String): FlashcardData {
        if (side == "back" || (side == "shuffle" && Random.nextBoolean())) {
            return FlashcardData(card.back, card.front)
        }
        return card
    }

    // ------------------------------------------------------------
    // ABSTRACT INTERFACE (Subclass Requirements)
    // ------------------------------------------------------------

    /**
     * createInstance
     * The "Constructor" used by the Factory.
     * Must return a new instance of the subclass, populated with 'data'.
     */
    protected abstract fun createInstance(data: TopicData?): AbstractFlashcardType

    // Helpers
    fun getTopics(): List<String> = database.keys.sorted()
}