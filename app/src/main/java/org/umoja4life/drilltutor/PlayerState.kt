package org.umoja4life.drilltutor

import kotlinx.serialization.Serializable

/**
 * PlayerState
 * Captures the pointers required to restore a session.
 * Matches Ruby's prep_serialize_settings hash structure.
 */
@Serializable
data class PlayerState(
    val topicKey: String,
    val curPtr: Int = 0,
    val groupDex: Int = 0,
    val showRear: Boolean = true, // Ruby: :rear
    val shuffleIndexes: List<Int> = emptyList()
)