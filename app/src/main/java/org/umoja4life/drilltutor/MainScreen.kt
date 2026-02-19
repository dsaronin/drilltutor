package org.umoja4life.drilltutor

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import org.umoja4life.drilltutor.ui.theme.DrillTutorTheme

// *****************************************************************
// The Screen UI Configuration Object
// Holds all data required to render the content area.
// *****************************************************************
data class ScreenConfiguration(
    val appTitle: String = "",
    val isListMode: Boolean = false,
    val isLessonMode: Boolean = false,
    val isListIconVisible: Boolean = false,
    val isAuxVisible: Boolean = false,
    val isTextMode: Boolean = false,
    val fontSize: DrillViewModel.CardFontSize = DrillViewModel.CardFontSize.NORMAL,
    val paddingValues: PaddingValues = PaddingValues(0.dp), // Default to 0; updated by Scaffold
    val currentCard: FlashcardData = FlashcardData(),
    val listData: List<FlashcardData> = emptyList()
)
// *****************************************************************
// the Actions Container
// *****************************************************************
data class DrillActions(
    val onNext: () -> Unit,
    val onPrev: () -> Unit,
    val onFlip: () -> Unit,
    val onShuffle: () -> Unit,
    val onNextGroup: () -> Unit,
    val onPrevGroup: () -> Unit,
    val onReset: () -> Unit,
    val onMenu: () -> Unit,
    val onToggleList: () -> Unit,
    val onLessonsClick: () -> Unit
)
// *****************************************************************
// *****************************************************************

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.drawer_header_height))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.flash_icon_2),
                contentDescription = stringResource(id = R.string.cd_logo),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_drawer_logo))
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))
            Text(
                text = stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DrillViewModel) {
    // ********************************************************
    // OBSERVE: These update automatically when ViewModel changes
    // ********************************************************
    val currentCard by viewModel.currentCard.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val appTitle by viewModel.appTitle.collectAsState()

    val isListMode by viewModel.isListMode.collectAsState()
    val isListIconVisible by viewModel.isListIconVisible.collectAsState()
    val listData by viewModel.listData.collectAsState()
    val isTextMode by viewModel.isTextMode.collectAsState()
    val isLessonMode by viewModel.isLessonMode.collectAsState()
    val auxTarget by viewModel.auxTarget.collectAsState()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ********************************************************
    // Lifecycle Observer for App Backgrounding
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.saveCurrentState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ********************************************************
    // Inside MainScreen
    val actions = DrillActions(
        onNext = { viewModel.onNextClick() },
        onPrev = { viewModel.onPrevClick() },
        onFlip = { viewModel.onFlipClick() },
        onShuffle = { viewModel.onShuffleClick() },
        onNextGroup = { viewModel.onNextGroupClick() },
        onPrevGroup = { viewModel.onPrevGroupClick() },
        onReset = { viewModel.onResetClick() },
        onMenu = { scope.launch { drawerState.open() } },
        onToggleList = { viewModel.onToggleListMode() },
        onLessonsClick = { viewModel.onToggleLessonMode() }
    )
    // ********************************************************

    // Establish ScreenConfiguration object
    val screenConfig = ScreenConfiguration(
        appTitle = appTitle,
        isListMode = isListMode,
        isLessonMode = isLessonMode,
        isListIconVisible = isListIconVisible,
        isAuxVisible = (auxTarget != null),
        isTextMode = isTextMode,
        fontSize = fontSize,
        currentCard = currentCard,
        listData = listData
    )
    // ********************************************************

    // ********************************************************

    // ********************************************************
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                navController = navController,
                scope = scope
            )
        }
    ) {
        DrillTutorNavHost(
            navController = navController,
            isLoading = isLoading,
            screenConfig = screenConfig,
            actions = actions
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrillTutorTopBar(config: ScreenConfiguration, actions: DrillActions) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_bar_vertical))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flash_icon_2),
                    contentDescription = stringResource(id = R.string.cd_logo),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_top_bar_logo))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                Text(
                    stringResource(id = R.string.app_name),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_bar_content_vertical))
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = actions.onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(id = R.string.cd_menu))
            }
        },
        actions = {
            // AUX icon button
            if (config.isAuxVisible) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = stringResource(id = R.string.cd_aux_link),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            IconButton(onClick = actions.onLessonsClick) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = stringResource(id = R.string.cd_icon_lessons),
                    tint = if (config.isLessonMode)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onPrimary
                )
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun DrillTutorBottomBar(
    config: ScreenConfiguration,
    actions: DrillActions
) {
    BottomAppBar(
        contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_bar_vertical))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = actions.onReset) {
                Icon(Icons.Filled.Stop, contentDescription = stringResource(id = R.string.cd_quit))
            }
            IconButton(onClick = actions.onPrevGroup) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(id = R.string.cd_group_previous))
            }
            IconButton(onClick = actions.onFlip) {
                Icon(Icons.Filled.RotateRight, contentDescription = stringResource(id = R.string.cd_flip_card))
            }
            IconButton(onClick = actions.onNextGroup) {
                Icon(Icons.Filled.SkipNext, contentDescription = stringResource(id = R.string.cd_group_next))
            }
            IconButton(onClick = actions.onShuffle) {
                Icon(Icons.Filled.Shuffle, contentDescription = stringResource(id = R.string.cd_shuffle))
            }

            if (config.isListIconVisible) {
                val tint = if (config.isListMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                IconButton(onClick = actions.onToggleList) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(id = R.string.cd_icon_lists),
                        tint = tint
                    )
                }
            }

        }
    }
}

@Composable
private fun DrillTutorContent(
    config: ScreenConfiguration,
    actions: DrillActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(config.paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (config.isLessonMode) {
                // Fetch Key from Global State
            val globalTopic = Environment.settings.settingState.value.topic
            var selectedLessonKey by androidx.compose.runtime.remember(globalTopic) {
                androidx.compose.runtime.mutableStateOf(prepLessonKey(globalTopic))
            }
            val allLessonKeys = androidx.compose.runtime.remember { getLessonKeys() }
            // val validKey = prepLessonKey(targetKey = globalTopic)
            val lessonData = prepLessonCard(selectedLessonKey)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                // Header: Interactive Dropdown
                LessonDropdownSelector(
                    currentSelection = selectedLessonKey,
                    allOptions = allLessonKeys,
                    onSelectionChange = { newKey -> selectedLessonKey = newKey }
                )

                LessonsView(
                    lessonData = lessonData,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {

            // Orientation subheading at the top of the workspace
            Text(
                text = config.appTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_small))
            )

            // The Logic Switch
            if (config.isListMode) {
                FlashcardListView(
                    listData = config.listData,
                    isTextMode = config.isTextMode
                )
            } else {
                FlashcardPlayerView(
                    config = config,
                    actions = actions
                )
            }

        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PortraitLayout(
        config: ScreenConfiguration,
        actions: DrillActions
    ) {
        Scaffold(
            topBar = { DrillTutorTopBar(config = config, actions = actions) },
            bottomBar = {
                DrillTutorBottomBar(config = config, actions = actions)
            }
        ) { innerPadding ->
            DrillTutorContent(
                config = config.copy(paddingValues = innerPadding),
                actions = actions
            )
        }
    }

    @Composable
    private fun LandscapeLayout(
        config: ScreenConfiguration,
        actions: DrillActions
    ) {
        val configuration = LocalConfiguration.current
        // In Landscape, the vertical edge matches the screen height.
        val sidebarLength = configuration.screenHeightDp.dp

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Sidebar (Masthead)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(dimensionResource(id = R.dimen.height_top_bar_landscape))
                        .zIndex(0f)
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    RotatedSideBar(sidebarLength) {
                        DrillTutorTopBar(config = config, actions = actions)
                    }
                }

                // Center Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .zIndex(1f), // <--- Ensure content sits above any sidebar overlap
                ) {
                    DrillTutorContent(config = config, actions = actions)
                }

                // Right Sidebar (Player)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(dimensionResource(id = R.dimen.height_bottom_bar_landscape))
                        .zIndex(0f)
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotated Container
                    RotatedSideBar(sidebarLength) {
                        DrillTutorBottomBar(
                            config = config,
                            actions = actions
                        )
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, widthDp = 360, heightDp = 640)
    @Composable
    fun MainScreenPreview() {
        DrillTutorTheme {
            // MainScreen()
            Text("Preview unavailable until state hoisting is implemented")
        }
    }

// [INSERT AT BOTTOM OF FILE]

@Composable
private fun FlashcardListView(
    listData: List<FlashcardData>,
    isTextMode: Boolean
) {
    // Scrollable container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(id = R.dimen.spacing_small))
    ) {
        if (isTextMode) {
            // SINGLE COLUMN (Text Mode)
            listData.forEach { card ->
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        } else {
            // TWO COLUMNS (Bullet Mode)
            // Split the list roughly in half
            val splitIndex = (listData.size + 1) / 2
            val leftList = listData.take(splitIndex)
            val rightList = listData.drop(splitIndex)

            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Column
                Column(modifier = Modifier.weight(1f)) {
                    leftList.forEach { card ->
                        BulletItem(text = card.front)
                    }
                }
                // Right Column (only if data exists)
                if (rightList.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        rightList.forEach { card ->
                            BulletItem(text = card.front)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            stringResource(id = R.string.bullet_char), // Bullet character
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AppDrawer(
    drawerState: DrawerState,
    navController: NavHostController,
    scope: kotlinx.coroutines.CoroutineScope
) {
    // We move the scroll state here because it belongs to the drawer content
    val scrollState = rememberScrollState()

    ModalDrawerSheet {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            DrawerHeader()
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.cd_icon_settings)) },
                label = { Text(stringResource(id = R.string.menu_settings), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_drawer_icon_text)), style = MaterialTheme.typography.titleLarge) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("settings")
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.primary
                )
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Info, contentDescription = stringResource(id = R.string.menu_about)) },
                label = { Text(stringResource(id = R.string.menu_about), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_drawer_icon_text)), style = MaterialTheme.typography.titleLarge) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate("about")
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.primary
                )
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = stringResource(id = R.string.menu_help)) },
                label = { Text(stringResource(id = R.string.menu_help), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_drawer_icon_text)), style = MaterialTheme.typography.titleLarge) },
                selected = false,
                onClick = { /*TODO*/ },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun DrillTutorNavHost(
    navController: NavHostController,
    isLoading: Boolean,
    screenConfig: ScreenConfiguration,
    actions: DrillActions
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            // LOADING STATE CHECK
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                if (isLandscape) {
                    LandscapeLayout(config = screenConfig, actions = actions)
                } else {
                    PortraitLayout(config = screenConfig, actions = actions)
                }
            }

        }
        composable("about") {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun RotatedSideBar(
    sidebarLength: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    // Rotated Container
    Box(
        modifier = Modifier
            .requiredWidth(sidebarLength) // <--- Use requiredWidth to ignore parent constraint
            .rotate(-90f),
        contentAlignment = Alignment.Center
    ) {
        content()
    }

}

    /**
     * Flashcard Player View
     *     // ADJUST HEIGHT FRACTION BASED ON ORIENTATION
     *     // Landscape: Use 85% of vertical space (since space is tight vertically).
     *     // Portrait: Use 40% of vertical space (standard look).
     *
     */
    @Composable
private fun FlashcardPlayerView(
    config: ScreenConfiguration,
    actions: DrillActions
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val cardHeightFraction = if (isLandscape) 0.85f else 0.6f
    // HELPER: Convert Dp resource to Sp for Text
    val density = androidx.compose.ui.platform.LocalDensity.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = actions.onPrev) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = stringResource(id = R.string.cd_previous_card),
                modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_small))
            )
        }
        Card(
            modifier = Modifier
                .height(screenHeight * cardHeightFraction)
                .weight(1f)
                .clickable { actions.onFlip() }    // tap card to flip it!
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Raw display of Front Data
                    // We will tackle dynamic font sizing in Step 1.2
                    Text(
                        text = config.currentCard.front,
                        // Direct access to the encapsulated ID
                        fontSize = with(density) {
                            dimensionResource(id = config.fontSize.dimenResId).toSp()
                        },
                        lineHeight = with(density) {
                            dimensionResource(id = config.fontSize.dimenResId).toSp()
                        },
                        fontWeight = FontWeight.Bold,
                        // style = MaterialTheme.typography.displayMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        IconButton(onClick = actions.onNext) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = stringResource(id = R.string.cd_next_card),
                modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_small))
            )
        }
    }
}

/**
 * prepLessonKey
 * Helpers to fetch the first available lesson or return a stub if data is missing.
 * Validates a requested key against the available Lesson topics.
 * Returns the target if valid, the first available if not, or empty string if no lessons exist.
 */
private fun prepLessonKey(targetKey: String): String {
    val handler = FlashcardTypeSelection.selectCardType(FlashcardSource.LESSONS)
    val topics = handler.getTopics() // Returns List<String> (keys)

    return when {
        targetKey in topics -> targetKey
        topics.isNotEmpty() -> topics[0]
        else -> ""
    }
}

/**
 * prepLessonCard
 * Fetches the Lesson Data for a vetted key.
 * If the key is empty or fetch fails, returns a standard Error Stub.
 */
@Composable
private fun prepLessonCard(vettedKey: String): TopicData {
    val handler = FlashcardTypeSelection.selectCardType(FlashcardSource.LESSONS)

    // Return data if key exists and fetch succeeds; otherwise return Error Stub
    return vettedKey.takeIf { it.isNotEmpty() }?.let { handler.getItem(it) }
        ?: createErrorLesson(stringResource(id = R.string.error_lessons_missing))
}

/**
 * getLessonKeys
 * Returns a sorted list of all available Lesson Keys.
 * Used to populate the navigation dropdown.
 */
private fun getLessonKeys(): List<String> {
    val handler = FlashcardTypeSelection.selectCardType(FlashcardSource.LESSONS)
    return handler.getTopics().sorted()
}

@Composable
private fun LessonDropdownSelector(
    currentSelection: String,
    allOptions: List<String>,
    onSelectionChange: (String) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        // The Trigger: Clickable Row with Text + Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(bottom = dimensionResource(id = R.dimen.spacing_small))
        ) {
            Text(
                text = currentSelection,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(id = R.string.cd_select_lesson),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // The Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allOptions.forEach { key ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (key == currentSelection) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelectionChange(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

