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
    // Window Title State
    private val _appTitle = MutableStateFlow("")
    val appTitle: StateFlow<String> = _appTitle.asStateFlow()

    // Dynamic Font Sizing for Front Text
    private val _fontSize = MutableStateFlow(CardFontSize.NORMAL)
    val fontSize: StateFlow<CardFontSize> = _fontSize.asStateFlow()

    // The Loading Flag (True = Show Spinner, False = Show Card)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- LIST VIEW STATE ---
    private val _isListMode = MutableStateFlow(false)
    val isListMode: StateFlow<Boolean> = _isListMode.asStateFlow()
    // --- LESSON MODE STATE ---
    private val _isLessonMode = MutableStateFlow(false)
    val isLessonMode: StateFlow<Boolean> = _isLessonMode.asStateFlow()


    private val _isListIconVisible = MutableStateFlow(false)
    val isListIconVisible: StateFlow<Boolean> = _isListIconVisible.asStateFlow()

    private val _listData = MutableStateFlow<List<FlashcardData>>(emptyList())
    val listData: StateFlow<List<FlashcardData>> = _listData.asStateFlow()

    private val _isTextMode = MutableStateFlow(false)
    val isTextMode: StateFlow<Boolean> = _isTextMode.asStateFlow()
    private var isFirstLoad = true  // true if first time loading data

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
        monitorRepository()  // Start monitoring flashcard repo loading
        monitorSettings() // Start monitoring settings
    }
    // ***********************************************************************
    // *****  PUBLIC ACTIONS  ***********************************************
    // ***********************************************************************
    fun onToggleLessonMode() {
        _isLessonMode.value = !_isLessonMode.value
    }


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
        prepCardDisplay(manager.flipCard())
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
    fun onToggleListMode() {
        _isListMode.value = !_isListMode.value
    }

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
    // Start FlashManager and prep CurrentCard
    // ***********************************************************************

    private fun prepCardDisplay(card: FlashcardData) {
        // Apply the source-specific formatting rules
        val formattedCard = applyDisplayFormatting(card)
        // variable font size card display
        _fontSize.value = CardFontSize.lengthToFontSize(formattedCard.front)
        _currentCard.value = formattedCard
    }

    private suspend fun rebuildManager(isConfigurationChange: Boolean) {
        Environment.logInfo("DrillVM: ReBuilding FlashManager; ConfigChange: $isConfigurationChange ...")

        // Reset state on config change; otherwise load from persistence
        playState = if (isConfigurationChange) {
            PlayerState() // Assumes default constructor creates a clean "zeroed" state
        } else {
            Environment.playerState.loadPlayerState()
        }

       // Instantiate the Logic Engine with fresh data
        flashManager = FlashManager(
            Environment.settings.settingState.value,
            playState!!
        )

        // POPULATE LIST DATA ---
        _listData.value = flashManager?.getListViewData() ?: emptyList()
        _isTextMode.value = flashManager?.textOrBullets() ?: false

        // Update the title to reflect the new Source/Topic
        _appTitle.value = formatTitle()
        prepCardDisplay(flashManager?.currentCard() ?: FlashcardData())  // preps currentCard for display refresh
    }

    // ***********************************************************************
    // Monitor changes in Settings and Loading Repository
    // ***********************************************************************
    private var currentLanguage: String = ""  // track language state
    private fun monitorSettings() {
        viewModelScope.launch {
            Environment.settings.settingState.collect { state ->
                // RESET UI STATE
                _isListMode.value = false
                _isLessonMode.value = false // <--- Force reset to Flashcard Player

                // Determine icon visibility
                // List View is NOT available for Dictionary
                _isListIconVisible.value = (state.source != FlashcardSource.DICTIONARY)

                // 1. Initialize tracker on first run
                if (currentLanguage.isNullOrEmpty()) {
                    currentLanguage = state.language
                    // Do not return; let the logic below decide if we need to build
                }

                // 2. CRITICAL FIX: IGNORE LANGUAGE CHANGES
                // If the language changed, a heavy Data Load is incoming.
                // We MUST wait for 'monitorRepository' (DataStatus.Ready) to trigger the rebuild.
                // If we rebuild now, we will be using the OLD data with the NEW settings.
                if (state.language != currentLanguage) {
                    Environment.logInfo("DrillVM: Language change detected (${currentLanguage} -> ${state.language}). Skipping config rebuild; waiting for Data Load.")
                    currentLanguage = state.language
                    return@collect
                }

                // 3. Handle Standard Config Changes (Topic, Size, etc.)
                // These are safe to apply immediately because the Data is unchanged.
                // [GUARD] Only rebuild if the Data is actually Ready.
                // 1. Prevents double-build on App Startup (avoids racing monitorRepository).
                // 2. Ensures we don't try to build a FlashManager with empty/loading data.
                if (Environment.flashcards.dataState.value == DataStatus.Ready) {
                    Environment.logInfo("DrillVM: Settings changed ($state).")
                    rebuildManager(isConfigurationChange = true)
                }
            }
        }
    }

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
                        // App Start (First Load) -> RESTORE old state (false).
                        // Language Change (Subsequent Load) -> RESET state to defaults (true).
                        if (isFirstLoad) {
                            rebuildManager(isConfigurationChange = false)
                            isFirstLoad = false
                        } else {
                            Environment.logInfo("DrillVM: Data Reloaded (Language Change). Resetting PlayerState.")
                            rebuildManager(isConfigurationChange = true)
                        }
                        _isLoading.value = false
                    }
                    DataStatus.Idle -> {
                        // Do nothing, waiting for trigger
                    }
                }
            }
        }
    }
    // ***********************************************************************
    // Display Prep & Formatting HELPERS
    // ***********************************************************************
    /**
     * formatTitle
     * Returns string: "Source • Topic"
     * Separator: [En Space][Bullet][En Space]
     */
    private fun formatTitle(): String {
        // Access .value because these are StateFlows
        val source = Environment.settings.settingState.value.source
        val topic = playState?.topicKey ?: ""

        // \u2002 = En Space
        // \u2022 = Bullet
        return "$source\u2002\u2022\u2002$topic"
    }
    /**
     * getParentSourcePath
     * Returns "SOURCE__KEY" string if the passed TopicData object has a valid parent.
     */
    private fun getParentSourcePath(obj: TopicData): String? {
        val list = obj.belongsTo
        if (list.isNullOrEmpty() || list.size < 2) return null

        val parentSourceName = list[0]
        val parentKey = list[1]

        if (parentKey.isEmpty()) return null

        // Validate Parent Source Class
        val parentSource = FlashcardSource.fromSourceName(parentSourceName)
        if (parentSource == FlashcardSource.UNKNOWN) return null

        // Validate Parent Entry Existence (The Cross-Reference Check)
        // Ruby: Module.const_get(source).get_item(key).nil?
        if (FlashcardTypeSelection.selectCardType(parentSource).getItem(parentKey) == null) return null

        return "${parentSourceName}__$parentKey"
    }

    /**
     * getGlossary
     * Returns a TopicData object for a referenced glossary
     */
    private fun getGlossary(obj: TopicData): TopicData?  {
        // Ruby: return nil if obj.has_glossary.nil?
        val glossaryKey = obj.hasGlossary
        if (glossaryKey.isNullOrEmpty()) return null  // no ref'd glossary

        // Ruby: source = Glossaries.find_glossary( obj.has_glossary )
        // We cast to GlossaryType to access the specific findGlossary method
        val handler = FlashcardTypeSelection.selectCardType(FlashcardSource.GLOSSARIES) as? GlossaryType
        val glossaryObj = handler?.findGlossary(glossaryKey)

        if (glossaryObj != null) {
            return glossaryObj
        }

        // TODO:  "Glossary <#{obj.has_glossary}> not found; typo?"
        return null
    }

    /**
     * getGlossaryPath
     * Returns "Glossaries__KEY" string if the passed TopicData object has a valid glossary.
     */
    private fun getGlossaryPath(obj: TopicData): String? {
        return getGlossary(obj)?.let {
            "Glossaries__${obj.hasGlossary}"
        }
    }

    /**
     * applyDisplayFormatting
     * Applies source-specific formatting rules (Regex, substitution)
     * to prepare the raw data for UI presentation.
     */
    private fun applyDisplayFormatting(rawCard: FlashcardData): FlashcardData {
        val source = Environment.settings.settingState.value.source

        return when (source) {
            FlashcardSource.DIALOGS -> {
                // Ruby: @front.gsub( /^.: / , "" )
                // Removes "A: " or "B: " prefixes
                val cleanFront = rawCard.front.replace(Regex("^.: "), "")
                rawCard.copy(front = cleanFront)
            }

            FlashcardSource.OPPOSITES -> {
                // Ruby: @front.gsub( /::/, " &harr; " )
                // Replaces "::" with Left-Right Arrow
                val formattedFront = rawCard.front.replace("::", " \u2194 ")
                rawCard.copy(front = formattedFront)
            }

            FlashcardSource.DICTIONARY -> {
                // Ruby: @rear.split( /; / )
                // Kotlin: Convert "; " to newlines to simulate a list in a String field
                val listRear = rawCard.back.replace("; ", "\n")
                rawCard.copy(back = listRear)
            }

            else -> rawCard // No changes for other types
        }
    }
    /**
     * mineExamples
     * Determines which side of the card to look at, extracts the key,
     * and finds example sentences.
     */
    private fun mineExamples(card: FlashcardData): List<String> {
        // Get the current side from Settings
        val currentSide = Environment.settings.settingState.value.cardSide

        val textToScan = when (currentSide) {  //  based on the side
            CardSide.FRONT -> card.front
            CardSide.BACK -> card.back
            else -> ""
        }

        if (textToScan.isEmpty()) return emptyList()

        // Extract the key
        val key = flashManager?.extractKey(textToScan) ?: return emptyList()

        // Return mined examples found
        return flashManager?.mineExamples(key) ?: emptyList()
    }
}