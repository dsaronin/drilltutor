package org.umoja4life.drilltutor

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Environment
 * The Central Nervous System.
 * - Identity (Version, Name)
 * - Service Locator (Access to Settings)
 * - Logging Utility
 */
object Environment {

    // --- IDENTITY ---
    const val APP_NAME = "DrillTutor"
    val VERSION_NAME: String = BuildConfig.VERSION_NAME
    val VERSION_CODE: String = BuildConfig.DRILLTUTOR_VERSION_CODE
    val IS_DEBUG: Boolean = BuildConfig.DEBUG

    const val LIST_CLAMP = 80  // max length for list view
    const val DEFAULT_TOPIC = "default"    // standard missing topic value
    val REGEX_DEFAULT = Regex("^(?i)def(ault)?$")

    // --- DATA SOURCE VALIDATION CONSTANTS ---
    val REGEX_LANG_DIR = Regex("^[a-z]{2}$")
    const val FILE_VOCABULARY = "vocabulary.json"
    const val FILE_LESSONS = "lessons.json"

    // --- LOGGING ---
    private const val LOG_TAG = "App" + APP_NAME

    // --- CONCURRENCY ---
    // A global scope for the app environment.
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- SUBSYSTEMS ---
    // The "lateinit" means we promise to initialize this in init() before using it.
    lateinit var appContext: Context
        private set

    lateinit var settings: SettingsRepository
        private set
    lateinit var flashcards: FlashcardRepository
        private set
    lateinit var playerState: PlayerStateRepository

    lateinit var emptyFlashcardData: List<FlashcardData>
        private set

    lateinit var storage: StorageStateRepository
        private set

    // --- INITIALIZATION ---
    fun init(context: Context) {
        // PREVENT MEMORY LEAK:
        // We ensure we only hold the Application Context, not an Activity Context.
        appContext = context.applicationContext

        // Initialize fallback data using string resources
        emptyFlashcardData = listOf(
            FlashcardData(
                front = appContext.getString(R.string.missing_front),
                back = appContext.getString(R.string.missing_back)
            )
        )

        logInfo("Environment Initializing...")
        logInfo("Version: $VERSION_NAME (Build $VERSION_CODE)")

        // Instantiate Settings subsystem
        settings = SettingsRepository(appContext)

        // Instantiate PlayerState subsystem
        playerState = PlayerStateRepository(appContext)

        // Instantiate Storage subsystem
        storage = StorageStateRepository(appContext)

        // Instantiate Flashcard data subsystem
        flashcards = FlashcardRepository(appContext)

        scope.launch {
            flashcards.bootup()  // get the settings, load the data
        }

        logInfo("Environment Initialized.")
    }


    // --- LOGGING UTILITIES ---
    fun logInfo(msg: String) = Log.i(LOG_TAG, msg)
    fun logDebug(msg: String) { if (IS_DEBUG) Log.d(LOG_TAG, "DEBUG: $msg") }
    fun logWarn(msg: String) = Log.w(LOG_TAG, "WARN: $msg")
    fun logError(msg: String) = Log.e(LOG_TAG, "ERROR: $msg")
}