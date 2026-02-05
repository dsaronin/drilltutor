package org.umoja4life.drilltutor

import kotlin.random.Random

/**
 * FlashManager
 * The "Hardware Controller" for flashcard playback.
 * It manages indices and asks the Source for the card at a specific index.
 * It DOES NOT hold the data list.
 */
class FlashManager {

    // STATE POINTERS
    var curPtr: Int = -1  // -1 if first time only
        private set

    var groupDex: Int = 0
        private set

    var showRear: Boolean = true
        private set

    // The working set of indices for the current group
    // It is this set which is either ordered or shuffled; never the
    // list of data itself.
    private var shuffleIndexes: MutableList<Int> = mutableListOf()


    // CONFIGURATION
    private var groupSize: Int = 5        // LINKAGE: SettingsRepository
    private var isOrdered: Boolean = true // LINKAGE: SettingsRepository


    // SOURCE REFERENCE
    // "Source" is an object of AbstractFlashcardType which holds the
    // data for the current topic and knows how to access it for the player.
    // mySource.fc_data is a list of FlashcardData objects
    private var mySettings: SettingsRepository? = null  // TODO: ensure never null
    private var myHandler: AbstractFlashcardType? = null  // TODO: ensure never null
    private var mySource: AbstractFlashcardType? = null  // TODO: ensure never null

    private var myTopicKey: String = ""
    private var maybeEntry: String? = null

    /**
     * prepSerializeSettings
     * Corresponds to Ruby: def prep_serialize_settings
     */
    fun prepSerializeSettings(): PlayerState {
        return PlayerState(
            topicKey = myTopicKey,
            curPtr = curPtr,
            groupDex = groupDex,
            showRear = showRear,
            shuffleIndexes = shuffleIndexes.toList()
        )
    }

    /**
     * updateConfig
     * Syncs working variables from SettingsRepository.
     */
    fun updateConfig(settings: SettingsRepository, playerSettings: PlayerState) {
        mySettings = settings

        // TODO: formalized retrieval from settings
        // curPtr = playerSettings.curPtr
        // groupDex = playerSettings.groupDex
        // showRear = playerSettings.showRear
        // shuffleIndexes = playerSettings.shuffleIndexes.toMutableList()
        // this.groupSize = settings.groupSize
        // this.isOrdered = settings.selector == "ordered"
        // myHandler = settings.source  // TODO: LINKAGE: AbstractFlashcardType
        // myTopicKey = settings.topic
        // maybeEntry = settings.entry
    }

    /**
     * initializeFlashManager
     */
    fun initializeFlashManager(settings: SettingsRepository) {
        updateConfig(settings, PlayerState())
    }

    /**
     * setMySource
     * Corresponds to Ruby: initialize(key, settings) logic where @my_source is found.
     */
    fun setMySource(topicKey: String) {
        var topic = topicKey.replace(":", "")

        // Assuming mySettings!!.currentTopic holds the default topic
        if (topic.matches(Regex("^def(ault)?$"))) {
            topic = mySettings?.currentTopic ?: "" // Safety fallthrough
        }

        Environment.logInfo("FLASHMGR: source: ${mySettings?.currentSource}, topic: $topic, entry: $maybeEntry")

        // VocabularyType is the source of truth for all topics.
        val validatedTopic = VocabularyType.findTopic(topic)

        if (validatedTopic == null) {
            Environment.logError("Topic: $topic not found")
            validatedTopic = VocabularyType.firstTopic()
        }

        myTopicKey = validatedTopic

        // myHandler is already the "Module", so we call findOrNew on it.
        // Note: findOrNew needs to return the specific handler instance for this topic
        val sourceInstance = myHandler?.findOrNew(myTopicKey, maybeEntry)

        if (sourceInstance == null) {
            Environment.logError("Source for topic: $myTopicKey not found")
            // TODO: fail gracefully with a default source
        }

        mySource = sourceInstance

        // When source changes, we effectively start fresh
        resetIfStart()
    }


    /**
     * resetIfStart
     *  resets internal state of indexes iff 1st time
     */
    fun resetIfStart() {
        if (curPtr == -1)) reset() else updateConfig()
    }

    /**
     * reset
     * resets all internals
     */
    fun reset() {
        unshuffle()
        groupDex = 0
        showRear = true
    }

    /**
     * setIndexes
     * Corresponds to Ruby: def set_indexes
     */
    private fun setIndexes(): Boolean {
        shuffleIndexes.clear()

        // Ruby logic: @my_source.fc_data.length
        // We access the map entry to get the size of the list
        val topicData = mySource?.getData()?.get(myTopicKey)
        val maxLen = topicData?.fcData?.size ?: 0

        if (maxLen == 0) return isOrdered

        if (isOrdered) {
            // Ruby: @shuffle_indexes = (0..(@my_settings[:sizer]-1)).to_a
            // Create range 0 to groupSize-1
            val limit = minOf(groupSize, maxLen)
            for (i in 0 until limit) {
                shuffleIndexes.add(i)
            }
        } else {
            // Ruby: @shuffle_indexes = random_selection(...)
            shuffleIndexes = randomSelection(maxLen, groupSize)
        }

        return isOrdered
    }

    // --- ADDITIONAL CONFIGURATION (From Settings) ---
    // Ruby: :side => SIDE_TYPES[0] ("front")
    // REQUIRED LINKAGE: SettingsRepository must provide the 'side' string (front/back/shuffle)
    private var side: String = "front"


    // ==========================================================
    // INSTANCE METHODS (Translation)
    // ==========================================================

    /**
     * unshuffle
     * Restores normal index order.
     */
    fun unshuffle() {
        curPtr = 0
        setIndexes() // Re-generates indexes based on isOrdered flag
    }

    /**
     * unshuffleCards
     */
    fun unshuffleCards(): FlashcardData {
        unshuffle()
        return currentCard()
    }

    /**
     * currentCard
     * The Extractor.
     */
    fun currentCard(): FlashcardData {
        val baseIndex = if (shuffleIndexes.isEmpty()) 0 else shuffleIndexes[curPtr]
        // Forced non-null assertion (!!) as per return type requirement.
        // Assumes mySource is initialized and valid.
        return mySource!!.getDataAtIndex((baseIndex + groupDex), side)
    }

    /**
     * resetCards
     */
    fun resetCards(): FlashcardData {
        reset()
        return currentCard()
    }

    /**
     * shuffleCards
     * Corresponds to Ruby: def shuffle_cards
     */
    fun shuffleCards(): FlashcardData {
        shuffleIndexes.shuffle(Random.Default)
        curPtr = 0
        return currentCard()
    }

    /**
     * nextCard
     */
    fun nextCard(): FlashcardData {
        // Ruby: if ( (@cur_ptr += 1) >= @my_settings[:sizer] )
        curPtr += 1
        if (curPtr >= groupSize) {
            curPtr = 0
        }
        return currentCard()
    }

    /**
     * prevCard
     */
    fun prevCard(): FlashcardData {
        // Ruby: if ( (@cur_ptr -= 1) < 0 )
        curPtr -= 1
        if (curPtr < 0) {
            // Ruby: @cur_ptr = @my_settings[:sizer] - 1
            curPtr = groupSize - 1
        }
        return currentCard()
    }

    /**
     * headCard
     */
    fun headCard(): FlashcardData {
        curPtr = 0
        return currentCard()
    }

    /**
     * nextGroupCard
     * Advances to next group.
     * If ordered: resets indexes and advances group pointer.
     * If shuffled: just grabs a new random group (via setIndexes implied logic).
     */
    fun nextGroupCard(): FlashcardData {
        // Ruby: if set_indexes
        if (setIndexes()) {
            // Ruby: len =  @my_source.list_size
            // REQUIRED LINKAGE: AbstractFlashcardType needs a way to get size.
            // We use our internal helper `getListSize()` defined in the previous step.
            val len = getListSize()
            val size = groupSize

            // Ruby: if ( ((@group_dex += size) + size) > len) @group_dex = len - size end
            groupDex += size
            if ((groupDex + size) > len) {
                groupDex = len - size
            }
        } // if ordered

        curPtr = 0
        return currentCard()
    }

    /**
     * prevGroupCard
     * if an ordered selection: always resets the index order
     * if a shuffled selection: always grabs a new random group
     */
    fun prevGroupCard(): FlashcardData {
        // Ruby: if set_indexes
        if (setIndexes()) {
            // Ruby: if ( (@group_dex -= @my_settings[:sizer]) < 0 )
            groupDex -= groupSize
            if (groupDex < 0) {
                groupDex = 0
            }
        } // if ordered

        curPtr = 0
        return currentCard()
    }

     private fun getListSize(): Int {
    // TODO: LINKAGE with Settings
        return groupSize // <-- temporary
    }

    /**
     * randomSelection
     * Corresponds to Ruby: def random_selection(max, size)
     */
    private fun randomSelection(max: Int, size: Int): MutableList<Int> {
        val allIndices = (0 until max).toMutableList()
        allIndices.shuffle(Random.Default)
        return allIndices.take(size).toMutableList()
    }

    // ==========================================================
    // MISSING METHODS (Placeholders per Criticism 5)
    // ==========================================================

    /**
     * mineExamples
     * Corresponds to Ruby: def mine_examples(key)
     */
    fun mineExamples(key: String): List<String> {
        // TODO: Implementation pending discussion
        return emptyList()
    }

    /**
     * extractKey
     * Corresponds to Ruby: def extract_key(str)
     */
    fun extractKey(str: String): String {
        // TODO: Implementation pending discussion
        return ""
    }

    /**
     * textOrBullets
     * Corresponds to Ruby: def text_or_bullets
     */
    fun textOrBullets(): Boolean {
        // TODO: Implementation pending discussion
        return false
    }

    /**
     * listable
     * Corresponds to Ruby: def listable?
     */
    fun listable(): Boolean {
        // TODO: Implementation pending discussion
        return true
    }
}