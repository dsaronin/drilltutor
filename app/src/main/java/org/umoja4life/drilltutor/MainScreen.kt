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
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.umoja4life.drilltutor.ui.theme.DrillTutorTheme

// *****************************************************************
// the Actions Container
// *****************************************************************
data class DrillActions(
    val onNext: () -> Unit,
    val onPrev: () -> Unit,
    val onFlip: () -> Unit,
    val onFront: () -> Unit,
    val onShuffle: () -> Unit,
    val onNextGroup: () -> Unit,
    val onPrevGroup: () -> Unit,
    val onReset: () -> Unit,
    val onMenu: () -> Unit
    // We can easily add onShuffle, onGroupNext, etc. here later
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
    // OBSERVE: These update automatically when ViewModel changes
    val currentCard by viewModel.currentCard.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val appTitle by viewModel.appTitle.collectAsState()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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

    // Inside MainScreen
    val actions = DrillActions(
        onNext = { viewModel.onNextClick() },
        onPrev = { viewModel.onPrevClick() },
        onFlip = { viewModel.onFlipClick() },
        onFront = { viewModel.onFrontClick() },
        onShuffle = { viewModel.onShuffleClick() },
        onNextGroup = { viewModel.onNextGroupClick() },
        onPrevGroup = { viewModel.onPrevGroupClick() },
        onReset = { viewModel.onResetClick() },
        onMenu = { scope.launch { drawerState.open() } }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
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
                        icon = { Icon(Icons.Default.Category, contentDescription = stringResource(id = R.string.cd_icon_titles)) },
                        label = { Text(stringResource(id = R.string.menu_titles), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_drawer_icon_text)), style = MaterialTheme.typography.titleLarge) },
                        selected = false,
                        onClick = { /*TODO*/ },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedIconColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = stringResource(id = R.string.cd_icon_lessons)) },
                        label = { Text(stringResource(id = R.string.menu_lessons), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_drawer_icon_text)), style = MaterialTheme.typography.titleLarge) },
                        selected = false,
                        onClick = { /*TODO*/ },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedIconColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(id = R.string.cd_icon_lists)) },
                        label = { Text(stringResource(id = R.string.menu_lists), modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_drawer_icon_text)), style = MaterialTheme.typography.titleLarge) },
                        selected = false,
                        onClick = { /*TODO*/ },
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
                        LandscapeLayout(
                            currentCard = currentCard,
                            fontSize = fontSize,
                            appTitle = appTitle,
                            actions = actions
                        )
                    } else {
                        PortraitLayout(
                            currentCard = currentCard,
                            fontSize = fontSize,
                            appTitle = appTitle,
                            actions = actions
                        )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrillTutorTopBar(onMenuClick: () -> Unit) {
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
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(id = R.string.cd_menu))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun DrillTutorBottomBar(actions: DrillActions) {
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
            IconButton(onClick = actions.onFront) {
                Icon(Icons.Filled.Visibility, contentDescription = stringResource(id = R.string.cd_toggle_mode))
            }
            IconButton(onClick = actions.onNextGroup) {
                Icon(Icons.Filled.SkipNext, contentDescription = stringResource(id = R.string.cd_group_next))
            }
            IconButton(onClick = actions.onShuffle) {
                Icon(Icons.Filled.Shuffle, contentDescription = stringResource(id = R.string.cd_shuffle))
            }

        }
    }
}

@Composable
private fun DrillTutorContent(
    paddingValues: PaddingValues,
    card: FlashcardData,   // for accessing card content
    fontSize: DrillViewModel.CardFontSize,
    appTitle: String,
    actions: DrillActions
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    // ADJUST HEIGHT FRACTION BASED ON ORIENTATION
    // Landscape: Use 85% of vertical space (since space is tight vertically).
    // Portrait: Use 40% of vertical space (standard look).
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val cardHeightFraction = if (isLandscape) 0.85f else 0.6f
    // HELPER: Convert Dp resource to Sp for Text
    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Orientation subheading at the top of the workspace
        Text(
            text = appTitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_small))
        )
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
                            text = card.front,
                            // Direct access to the encapsulated ID
                            fontSize = with(density) {
                                dimensionResource(id = fontSize.dimenResId).toSp()
                            },
                            lineHeight = with(density) {
                                dimensionResource(id = fontSize.dimenResId).toSp()
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PortraitLayout(
        currentCard: FlashcardData,
        fontSize: DrillViewModel.CardFontSize,
        appTitle: String,
        actions: DrillActions
    ) {
        Scaffold(
            topBar = { DrillTutorTopBar(onMenuClick = actions.onMenu) },
            bottomBar = { DrillTutorBottomBar(actions = actions) }
        ) { innerPadding ->
            DrillTutorContent(
                paddingValues = innerPadding,
                card = currentCard,
                fontSize = fontSize,
                appTitle = appTitle,
                actions = actions
            )
        }
    }

    @Composable
    private fun LandscapeLayout(
        currentCard: FlashcardData,
        fontSize: DrillViewModel.CardFontSize,
        appTitle: String,
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
                    // Rotated Container
                    Box(
                        modifier = Modifier
                            .requiredWidth(sidebarLength) // <--- Use requiredWidth to ignore parent constraint
                            .rotate(-90f),
                        contentAlignment = Alignment.Center
                    ) {
                        DrillTutorTopBar(onMenuClick = actions.onMenu)
                    }
                }

                // Center Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .zIndex(1f), // <--- Ensure content sits above any sidebar overlap
                ) {
                    DrillTutorContent(
                        paddingValues = PaddingValues(0.dp),
                        card = currentCard,
                        fontSize = fontSize,
                        appTitle = appTitle,
                        actions = actions
                    )
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
                    Box(
                        modifier = Modifier
                            .requiredWidth(sidebarLength) // <--- Use requiredWidth to ignore parent constraint
                            .rotate(-90f),
                        contentAlignment = Alignment.Center
                    ) {
                        DrillTutorBottomBar(actions = actions)
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