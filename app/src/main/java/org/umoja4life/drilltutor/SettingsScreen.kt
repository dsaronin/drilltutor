package org.umoja4life.drilltutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp

//import org.umoja4life.drilltutor.ui.theme.Gray050
//import org.umoja4life.drilltutor.ui.theme.TurkiyeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    // Observe the Unified State Object
    val state by viewModel.settings.collectAsState()

    val largeFontSize: TextUnit = with(LocalDensity.current) {
        // Correct syntax: value.toSp()

        dimensionResource(id = R.dimen.font_large).toSp()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary,
                    actionIconContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding) // Handle Scaffold padding
                .padding(dimensionResource(id = R.dimen.screen_padding))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_large))
        ) {

            // --- FORM FIELDS ---
            // 1. Source
            SimpleDropdown(
                label = stringResource(R.string.settings_label_source),
                currentValue = state.source.name,
                options = viewModel.availableSources,
                optionLabel = { it.sourceName },
                onOptionSelected = { viewModel.setSource(it) },
                fontSize = largeFontSize
            )


            // ---2. TOPIC DROPDOWN ---
            var expanded by remember { mutableStateOf(false) } // State to manage open/closed menu

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.topic, // Binds to the current topic state
                    onValueChange = {}, // Read-only, user must select from list
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_label_topic)) },
                    textStyle = LocalTextStyle.current.copy(fontSize = largeFontSize),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), // Matches other fields style
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor() // REQUIRED: links the text field to the menu
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Iterate through the topics from ViewModel
                    viewModel.availableTopics.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(text = selectionOption, fontSize = largeFontSize) },
                            onClick = {
                                viewModel.setTopic(selectionOption) // Save selection
                                expanded = false // Close menu
                            }
                        )
                    }
                }
            }

            // Part 3, Step 3: Localized ExposedDropdown for Selection
            if (viewModel.isSelectionVisible) {
                var expandedSelection by remember { mutableStateOf(false) }
                val noneLabel = stringResource(R.string.settings_label_none)

                ExposedDropdownMenuBox(
                    expanded = expandedSelection,
                    onExpandedChange = { expandedSelection = !expandedSelection },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (state.entryKey.isEmpty()) noneLabel else state.entryKey,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_label_selection)) },
                        textStyle = LocalTextStyle.current.copy(fontSize = largeFontSize),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSelection) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedSelection,
                        onDismissRequest = { expandedSelection = false }
                    ) {
                        // Prepend the empty string for the "None" option
                        val dynamicOptions = listOf("") + viewModel.availableSelections
                        dynamicOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (selectionOption.isEmpty()) noneLabel else selectionOption,
                                        fontSize = largeFontSize
                                    )
                                },
                                onClick = {
                                    viewModel.setEntryKey(selectionOption)
                                    expandedSelection = false
                                }
                            )
                        }
                    }
                }
            }

            // Visual break between Topic and Order logic
            HorizontalDivider(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.spacing_large)),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 3. Selection Order
            SimpleDropdown(
                label = stringResource(R.string.settings_label_order),
                currentValue = state.selector.name,
                options = viewModel.availableSelectors,
                optionLabel = { it.id },
                onOptionSelected = { viewModel.setSelector(it) },
                fontSize = largeFontSize
            )

            // 4. Group Size
            SimpleDropdown(
                label = stringResource(R.string.settings_label_size),
                currentValue = state.groupSize.toString(),
                options = viewModel.availableSizes,
                optionLabel = { it.toString() },
                onOptionSelected = { viewModel.setGroupSize(it) },
                fontSize = largeFontSize
            )

            // 5. Card Side
            SimpleDropdown(
                label = stringResource(R.string.settings_label_side),
                currentValue = state.cardSide.name,
                options = viewModel.availableSides,
                optionLabel = { it.id },
                onOptionSelected = { viewModel.setCardSide(it) },
                fontSize = largeFontSize
            )

            // 6. Language
            SimpleDropdown(
                label = stringResource(R.string.settings_label_language),
                currentValue = state.language.uppercase(),
                options = viewModel.availableLanguages,
                optionLabel = { it.uppercase() },
                onOptionSelected = { viewModel.setLanguage(it) },
                fontSize = largeFontSize
            )

        }
    }
}

/**
 * Updated SimpleDropdown with Deprecation Suppression
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleDropdown(
    label: String,
    currentValue: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            textStyle = LocalTextStyle.current.copy(fontSize = fontSize),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            fontSize = fontSize
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}