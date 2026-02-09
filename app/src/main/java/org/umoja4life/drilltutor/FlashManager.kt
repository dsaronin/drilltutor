package org.umoja4life.drilltutor

import kotlin.random.Random

/**
 * FlashManager
 * The "Hardware Controller" for flashcard playback.
 * It manages indices and asks the Source for the card at a specific index.
 * It DOES NOT hold the data list.
 */
class FlashManager (
    private var mySettings: SettingState,
    private var myState: PlayerState
){
    // *********************************************************************
    // ********  LOCAL CONFIGURATION  **********************
    // *********************************************************************
    private var groupSize: Int = 5        // any init value is ok; updateConfig always sets
    private var isOrdered: Boolean = true // any init value is ok; updateConfig always sets
    private var maybeEntry: String? = null
    private var side: String = "front"   // any init value is ok; updateConfig always sets



    // The working set of indices for the current group
    // It is this set which is either ordered or shuffled; never the
    // list of data itself.
    private var shuffleIndexes: MutableList<Int> = mutableListOf()

    /**
     * myHandler (The "Class" / Factory), Acts like the Class in Ruby
     *   but is a kotlin singleton instance. Used to call class methods.
     * mySource Is the Instantiated Object for a specific topic;
     *   which we used to access the array of flashcards, and thus yield
     *   a flashcard tuple for display.
     */
    private var myHandler: AbstractFlashcardType? = null  // TODO: ensure never null
    private var mySource: AbstractFlashcardType? = null  // TODO: ensure never null

    // *********************************************************************
    init {
        updateConfig()  // restore/update the player state based off of current settings values
    }
    // *********************************************************************

    /**
     * updateConfig
     * restore/update the player state based off of current settings values
     * invoked whenever Settings change
     */
    fun updateConfig() {

        // grab local store of current Settings
        groupSize = mySettings.groupSize
        isOrdered = mySettings.selector == SelectorType.ORDERED
        side = mySettings.cardSide.id
        maybeEntry = null

        myHandler = FlashcardTypeSelection.selectCardType(mySettings.source)

        setMySource(mySettings.topic)  // sets source; handler
    }

    /**
     * setMySource
     * Corresponds to Ruby: initialize(key, settings) logic where @my_source is found.
     */
    fun setMySource(topicKey: String) {
        var topic = topicKey.replace(":", "")

        // Assuming mySettings!!.currentTopic holds the default topic
        if (topic.matches(Regex("^def(ault)?$"))) {
            topic = mySettings.topic
        }

        Environment.logInfo("FLASHMGR: source: ${mySettings.source}, topic: $topic, entry: $maybeEntry")

        // VocabularyType is the source of truth for all topics.
        var validatedTopic = topic
        val availableTopics = myHandler?.getTopics() ?: emptyList()

        if (!availableTopics.contains(topic)) {
            Environment.logError("Topic: $topic not found")
            validatedTopic = availableTopics.firstOrNull() ?: ""
        }

        myState.topicKey = validatedTopic

        // myHandler is already the "Module", so we call findOrNew on it.
        // Note: findOrNew needs to return the specific handler instance for this topic
        val sourceInstance = myHandler?.findOrNew(myState.topicKey, maybeEntry)

        if (sourceInstance == null) {
            Environment.logError("Source for topic: ${myState.topicKey} not found")
            // TODO: fail gracefully with a default source
        }

        mySource = sourceInstance

        // When source changes, we effectively start fresh
        resetIfStart()
    }

    /**
     * resetIfStart
     * resets internal state of indexes iff 1st time
     */
    fun resetIfStart() {
        if (myState.curPtr == -1) reset() else setIndexes()
    }

    /**
     * reset
     * resets all internals
     */
    fun reset() {
        unshuffle()
        myState.groupDex = 0
        myState.showRear = true
    }

    /**
     * setIndexes
     * Corresponds to Ruby: def set_indexes
     */
    private fun setIndexes(): Boolean {
        shuffleIndexes.clear()

        // Ruby logic: @my_source.fc_data.length
        // We access the map entry to get the size of the list
        val maxLen = mySource?.listSize() ?: 0

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

    /**
     * unshuffle
     * Restores normal index order.
     */
    fun unshuffle() {
        myState.curPtr = 0
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
        val baseIndex = if (shuffleIndexes.isEmpty()) 0 else shuffleIndexes[myState.curPtr]
        // Forced non-null assertion (!!) as per return type requirement.
        // Assumes mySource is initialized and valid.
        return mySource!!.getDataAtIndex((baseIndex + myState.groupDex), side)
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
        myState.curPtr = 0
        return currentCard()
    }

    /**
     * nextCard
     */
    fun nextCard(): FlashcardData {
        // Ruby: if ( (@cur_ptr += 1) >= @my_settings[:sizer] )
        myState.curPtr += 1
        if (myState.curPtr >= groupSize) {
            myState.curPtr = 0
        }
        return currentCard()
    }

    /**
     * prevCard
     */
    fun prevCard(): FlashcardData {
        // Ruby: if ( (@cur_ptr -= 1) < 0 )
        myState.curPtr -= 1
        if (myState.curPtr < 0) {
            // Ruby: @cur_ptr = @my_settings[:sizer] - 1
            myState.curPtr = groupSize - 1
        }
        return currentCard()
    }

    /**
     * headCard
     */
    fun headCard(): FlashcardData {
        myState.curPtr = 0
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
            myState.groupDex += size
            if ((myState.groupDex + size) > len) {
                myState.groupDex = len - size
            }
        } // if ordered

        myState.curPtr = 0
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
            myState.groupDex -= groupSize
            if (myState.groupDex < 0) {
                myState.groupDex = 0
            }
        } // if ordered

        myState.curPtr = 0
        return currentCard()
    }

    private fun getListSize(): Int {
        return mySource?.listSize() ?: 0
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
    // MISSING METHODS (Placeholders)
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
