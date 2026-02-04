package org.umoja4life.drilltutor

import kotlin.random.Random

/**
 * FlashManager
 * The "Hardware Controller" for flashcard playback.
 * It manages indices and asks the Source for the card at a specific index.
 * It DOES NOT hold the data list.
 */
class FlashManager {

    // --- 1. STATE POINTERS (Ruby Instance Variables) ---
    var curPtr: Int = 0
        private set

    var groupDex: Int = 0
        private set

    var showRear: Boolean = true
        private set

    // The working set of indices for the current group
    // Ruby: @shuffle_indexes
    private var shuffleIndexes: MutableList<Int> = mutableListOf()


    // --- 2. CONFIGURATION (Ruby Settings) ---
    private var groupSize: Int = 5        // Ruby: :sizer
    private var isOrdered: Boolean = true // Ruby: derived from :selector


    // --- 3. SOURCE REFERENCE (The Subtype Object) ---
    // Ruby: @my_source
    // We hold the handler + key to delegate access.
    private var mySource: AbstractFlashcardType? = null
    private var myTopicKey: String = ""


    // ==========================================================
    // INPUT CHANNELS
    // ==========================================================

    /**
     * updateConfig
     * Syncs working variables from SettingsRepository.
     */
    fun updateConfig(settings: SettingsRepository) {
        // TODO: formalized retrieval from settings
        // this.groupSize = settings.groupSize
        // this.isOrdered = settings.selector == "ordered"
    }

    /**
     * setSource
     * Corresponds to Ruby: initialize(key, settings) logic where @my_source is found.
     */
    fun setSource(handler: AbstractFlashcardType, topicKey: String) {
        // Optimization: Only reset if topic actually changed
        if (this.myTopicKey == topicKey && this.mySource == handler) {
            return
        }

        this.mySource = handler
        this.myTopicKey = topicKey

        // When source changes, we effectively start fresh
        reset()
    }

    /**
     * resetIfStart
     * Corresponds to Ruby: def reset_if_start
     */
    fun resetIfStart(state: PlayerState?) {
        if (state != null && state.topicKey == myTopicKey) {
            // Restore from user's previous session
            this.curPtr = state.curPtr
            this.groupDex = state.groupDex
            this.showRear = state.showRear

            if (state.shuffleIndexes.isNotEmpty()) {
                this.shuffleIndexes = state.shuffleIndexes.toMutableList()
            } else {
                setIndexes()
            }
        } else {
            reset()
        }
    }


    // ==========================================================
    // LOGIC
    // ==========================================================

    /**
     * reset
     * Corresponds to Ruby: def reset
     */
    fun reset() {
        groupDex = 0
        showRear = true
        setIndexes()
        curPtr = 0
    }

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

    /**
     * randomSelection
     * Corresponds to Ruby: def random_selection(max, size)
     */
    private fun randomSelection(max: Int, size: Int): MutableList<Int> {
        val allIndices = (0 until max).toMutableList()
        allIndices.shuffle(Random.Default)
        return allIndices.take(size).toMutableList()
    }
}