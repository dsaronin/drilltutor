package org.umoja4life.drilltutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrillViewModel : ViewModel() {

    // ***********************************************************************
    // --- UI STATE ---
    // The specific card content to display
    private val _currentCard = MutableStateFlow(FlashcardData("", ""))
    val currentCard: StateFlow<FlashcardData> = _currentCard.asStateFlow()

    // Dynamic Font Sizing for Front Text
    private val _fontSize = MutableStateFlow(CardFontSize.NORMAL)
    val fontSize: StateFlow<CardFontSize> = _fontSize.asStateFlow()

    // The Loading Flag (True = Show Spinner, False = Show Card)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ***********************************************************************
    // --- LOGIC ENGINE ---
    // Nullable because it is invalid while data is loading
    private var flashManager: FlashManager? = null

    // Retain reference to the state object for persistence
    private var playState: PlayerState? = null

    // ***********************************************************************
    // Enum for Size Categories (Mapped to Dimens in UI)
    // ***********************************************************************
    enum class CardFontSize(val dimenResId: Int) {
        HUGE(R.dimen.font_size_huge),
        LARGE(R.dimen.font_size_large),
        BIG1(R.dimen.font_size_big_1),
        BIG2(R.dimen.font_size_big_2),
        BIG3(R.dimen.font_size_big_3),
        NORMAL(R.dimen.font_size_normal);

        companion object {
            /**
             * lengthToFontSize
             * Maps character count to font size category.
             * Legacy Ruby: 1-5=Huge, 6-8=Large, 9-16=Big1, 17-39=Big2, 40-59=Big3, Else=Normal
             */
            fun lengthToFontSize(text: String): CardFontSize {
                return when (text.length) {
                    in 1..5   -> HUGE
                    in 6..8   -> LARGE
                    in 9..16  -> BIG1
                    in 17..39 -> BIG2
                    in 40..59 -> BIG3
                    else      -> NORMAL
                }
            }
        }
    }
    // ***********************************************************************
    init {
        monitorRepository()
    }
    // ***********************************************************************
    // *****  PUBLIC ACTIONS  ***********************************************
    // ***********************************************************************

    fun onNextClick() {
        // Guard: Do nothing if manager isn't ready
        val manager = flashManager ?: return
        prepCardDisplay(manager.nextCard())
    }

    fun onPrevClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.prevCard())
    }

    fun onFlipClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.currentCard().flip())
    }

    fun onFrontClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.currentCard())
    }

    fun onShuffleClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.shuffleCards() )
    }

    fun onNextGroupClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.nextGroupCard() )
    }

    fun onPrevGroupClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.prevGroupCard() )
    }

    fun onResetClick() {
        val manager = flashManager ?: return
        prepCardDisplay(manager.resetCards() )
    }

    // ***********************************************************************
    // --- LIFECYCLE ---
    // ***********************************************************************

    override fun onCleared() {
        super.onCleared()
        saveCurrentState() // Trigger: App Closing / ViewModel Destruction
    }

    // ***********************************************************************
    // --- PERSISTENCE ---
    // ***********************************************************************

    fun saveCurrentState() {
        val state = playState ?: return

        viewModelScope.launch {
            Environment.playerState.savePlayerState(state)
        }
    }

    // ***********************************************************************
    private fun monitorRepository() {
        viewModelScope.launch {
            // OBSERVE: Listen to the Singleton Repository's status stream.
            // This fires on App Start AND when Settings changes the language.
            Environment.flashcards.dataState.collect { status ->
                when (status) {
                    DataStatus.Loading -> {
                        _isLoading.value = true
                    }
                    DataStatus.Ready -> {
                        rebuildManager()
                        _isLoading.value = false
                    }
                    DataStatus.Idle -> {
                        // Do nothing, waiting for trigger
                    }
                }
            }
        }
    }

    private fun prepCardDisplay(card: FlashcardData) {
        _fontSize.value = CardFontSize.lengthToFontSize(card.front)  // variable font size card display
        _currentCard.value = card
    }

    private suspend fun rebuildManager() {
        Environment.logInfo("DrillVM: (Re)Building FlashManager...")

        playState = Environment.playerState.loadPlayerState()

        // Instantiate the Logic Engine with fresh data
        flashManager = FlashManager(
            Environment.settings,
            playState!!
        )

        prepCardDisplay(flashManager?.currentCard() ?: FlashcardData())  // preps currentCard for display refresh
    }

}