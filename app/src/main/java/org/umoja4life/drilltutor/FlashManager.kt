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
    private var maybeEntry: String? = null  // related to mySetting.itemKey
    private var side: String = "front"   // any init value is ok; updateConfig always sets
    private var isFlipped: Boolean = false // true if card one-off flipped; updateConfig always sets



    // The working set of indices for the current group
    // It is this set which is either ordered or shuffled; never the
    // list of data itself.
    private var shuffleIndexes: MutableList<Int> = mutableListOf()

    // *********************************************************************
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
        maybeEntry = mySettings.entryKey.ifEmpty { null }
        isFlipped = false   // always normal side showing

        myHandler = FlashcardTypeSelection.selectCardType(mySettings.source)

        setMySource(mySettings.topic)  // sets source; handler
    }

    /**
     * setMySource
     * Corresponds to Ruby: initialize(key, settings) logic where @my_source is found.
     */
    fun setMySource(topicKey: String) {
        var topic = topicKey.replace(":", "")

        // GET MASTER LIST of availableTopics from Vocabulary
        val availableTopics = Environment.flashcards.getTopics(FlashcardSource.VOCABULARY)

        // VALIDATE & FALLBACK to first item in list
        // Condition A: Request is "def" or "default" (Explicit default)
        // Condition B: Request is not in the Master List (Invalid/Stale topic)
        if (topic.matches(Regex("^(?i)def(ault)?$")) || !availableTopics.contains(topic)) {
            val fallback = availableTopics.firstOrNull() ?: "default"
            Environment.logInfo("FLASHMGR: Topic '$topic' invalid/default. Falling back to '$fallback'")
            topic = fallback
        }

        // UPDATE STATE
        myState.topicKey = topic

        // Get the object corresponding to the topic
        val sourceInstance = myHandler?.findOrNew(myState.topicKey, maybeEntry)

        if (sourceInstance == null) {
            Environment.logError("FLASHMGR: Error - Could not get mySource for ${myState.topicKey}")
        }

        mySource = sourceInstance
        // When the source changes, the old 'curPtr' (e.g., 4) is likely invalid
        // for the new data (size 1). We must reset the pointer.
        // Even if we don't fully reset(), we must clamp curPtr or set it to 0.
        myState.curPtr = 0

        Environment.logInfo("FLASHMGR: source: ${mySettings.source}, topic: ${myState.topicKey}, entry: $maybeEntry")

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
        isFlipped = false   // reset normal side showing
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
        isFlipped = false   // reset normal side showing
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
        isFlipped = false   // reset normal side showing
        myState.curPtr = 0
        return currentCard()
    }

    /**
     * flipCard
     * one-off flipping card toggle
     */
    fun flipCard(): FlashcardData {
        isFlipped = !isFlipped  // Toggle the flag
        return if (isFlipped) currentCard().flip() else currentCard()
    }


    /**
     * nextCard
     */
    fun nextCard(): FlashcardData {
        // Ruby: if ( (@cur_ptr += 1) >= @my_settings[:sizer] )
        isFlipped = false   // reset normal side showing
        myState.curPtr += 1
        // FIX: Wrap based on actual available indices, not theoretical group size
        val limit = if (shuffleIndexes.isNotEmpty()) shuffleIndexes.size else groupSize

        if (myState.curPtr >= limit) {
            myState.curPtr = 0
        }
        return currentCard()
    }

    /**
     * prevCard
     */
    fun prevCard(): FlashcardData {
        isFlipped = false   // reset normal side showing
        // Ruby: if ( (@cur_ptr -= 1) < 0 )
        myState.curPtr -= 1
        if (myState.curPtr < 0) {
            // FIX: Wrap to the last actual index
            val limit = if (shuffleIndexes.isNotEmpty()) shuffleIndexes.size else groupSize
            myState.curPtr = limit - 1        }
        return currentCard()
    }

    /**
     * headCard
     */
    fun headCard(): FlashcardData {
        isFlipped = false   // reset normal side showing
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
        isFlipped = false   // reset normal side showing
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
        isFlipped = false   // reset normal side showing
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
    /**
     * mineExamples
     * Corresponds to Ruby: def mine_examples(key)
     * Search all available sources for usages of the given key (word/phrase).
     */
    fun mineExamples(key: String): List<String> {
        if (key.isEmpty()) return emptyList()

        val list = mutableListOf<String>()

        // Case 1: If we are specifically in DICTIONARY mode, we only look there.
        // (Assuming FlashcardSource.DICTIONARY exists in your Enum)
        if (mySettings.source == FlashcardSource.DICTIONARY) {
            val handler = FlashcardTypeSelection.selectCardType(FlashcardSource.DICTIONARY)
            list.addAll(handler.mineExamples(key))
        }
        // Case 2: Standard Study Mode - Mine ALL sources for examples.
        else {
            FlashcardSource.entries.forEach { source ->
                val handler = FlashcardTypeSelection.selectCardType(source)
                // We accumulate examples from every source (Sentences, Phrases, etc.)
                list.addAll(handler.mineExamples(key))
            }
        }

        return list
    }

    /**
     * textOrBullets
     * Corresponds to Ruby: def text_or_bullets
     * Returns true if the content should be displayed as running text (no bullets).
     * Ruby: TEXT_TYPES = %w{Dialogs Readings}
     */
    fun textOrBullets(): Boolean {
        return when (mySettings.source) {
            FlashcardSource.DIALOGS,
            FlashcardSource.READINGS -> true
            else -> false
        }
    }

    /**
     * listable
     * Corresponds to Ruby: def listable?
     * Returns true if the source supports listing/drilling behavior.
     * Ruby: LIST_TYPES = %w{Vocabulary Sentences Phrases Opposites Readings Dialogs Glossaries}
     * (Notably excludes DICTIONARY)
     */
    fun listable(): Boolean {
        return when (mySettings.source) {
            FlashcardSource.VOCABULARY,
            FlashcardSource.SENTENCES,
            FlashcardSource.PHRASES,
            FlashcardSource.OPPOSITES,
            FlashcardSource.READINGS,
            FlashcardSource.DIALOGS,
            FlashcardSource.GLOSSARIES -> true
            else -> false // e.g. DICTIONARY
        }
    }

    /**
     * extractKey
     * Extracts a meaningful keyword from a string to be used to search for examples.
     */
    fun extractKey(str: String): String {
        // Dictionary: Pass-through
        if (mySettings.source == FlashcardSource.DICTIONARY)  return str

        // Filter Source: Only allow Vocabulary or Opposites
        if (mySettings.source != FlashcardSource.VOCABULARY &&
            mySettings.source != FlashcardSource.OPPOSITES) {
            return ""
        }

        // Sanitize: Replace punctuation with space
        // We escape the dash '-' to ensure it's treated as a literal inside the character class.
        val punctuation = Regex("[:;.,\\-=+?!|~^$#@&*<>]")
        val cleanStr = str.replace(punctuation, " ")

        // Split: Break into words, ignoring multiple spaces
        val keys = cleanStr.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        // Constraints: Return empty if nothing left or too complex (> 6 words)
        if (keys.isEmpty() || keys.size > 6) return ""

        // Return original string if it's a short phrase (< 3 words)
        if (keys.size < 3) return str

        // Return the longest word
        return keys.maxByOrNull { it.length } ?: ""
    }

}
