package org.umoja4life.drilltutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.umoja4life.drilltutor.ui.theme.Gray050
import org.umoja4life.drilltutor.ui.theme.Gray700
import org.umoja4life.drilltutor.ui.theme.TurkiyeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val topic by viewModel.currentTopic.collectAsState()
    val source by viewModel.currentSource.collectAsState()
    val selector by viewModel.currentSelector.collectAsState()
    val size by viewModel.currentSize.collectAsState()
    val side by viewModel.currentSide.collectAsState()

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

            // --- TOPIC DROPDOWN ---
            var expanded by remember { mutableStateOf(false) } // State to manage open/closed menu

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = topic, // Binds to the current topic state
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

            // 2. Source
            SimpleDropdown(
                label = stringResource(R.string.settings_label_source),
                currentValue = source.id,
                options = viewModel.availableSources,
                optionLabel = { it.id },
                onOptionSelected = { viewModel.setSource(it) },
                fontSize = largeFontSize
            )

            // 3. Selection Order
            SimpleDropdown(
                label = stringResource(R.string.settings_label_order),
                currentValue = selector.id,
                options = viewModel.availableSelectors,
                optionLabel = { it.id },
                onOptionSelected = { viewModel.setSelector(it) },
                fontSize = largeFontSize
            )

            // 4. Group Size
            SimpleDropdown(
                label = stringResource(R.string.settings_label_size),
                currentValue = size.toString(),
                options = viewModel.availableSizes,
                optionLabel = { it.toString() },
                onOptionSelected = { viewModel.setGroupSize(it) },
                fontSize = largeFontSize
            )

            // 5. Card Side
            SimpleDropdown(
                label = stringResource(R.string.settings_label_side),
                currentValue = side.id,
                options = viewModel.availableSides,
                optionLabel = { it.id },
                onOptionSelected = { viewModel.setCardSide(it) },
                fontSize = largeFontSize
            )

            // 6. Language
            SimpleDropdown(
                label = stringResource(R.string.settings_label_language),
                currentValue = language.uppercase(),
                options = viewModel.availableLanguages,
                optionLabel = { it.uppercase() },
                onOptionSelected = { viewModel.setLanguage(it) },
                fontSize = largeFontSize
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xlarge)))

            // --- ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cancel Button
                TextButton(onClick = { onNavigateBack() }) {
                    Text(
                        text = stringResource(R.string.btn_cancel),
                        fontSize = largeFontSize,
                        color = Gray050
                    )
                }

                // Start Player Button
                Button(
                    onClick = { onNavigateBack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TurkiyeRed,
                        contentColor = Gray050
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_start),
                        fontSize = largeFontSize,
                        color = Gray050
                    )
                }
            }
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