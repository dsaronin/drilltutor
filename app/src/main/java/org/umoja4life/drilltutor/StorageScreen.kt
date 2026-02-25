package org.umoja4life.drilltutor

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    viewModel: StorageViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.storageState.collectAsState()
    val context = LocalContext.current

    // SAF Directory Chooser Launcher
    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable permission so we can read files after app restarts
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)

            // Save the new URI
            viewModel.updateStorageUri(it.toString())
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.storage_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back) // Assumes this exists from SettingsScreen
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
                .padding(innerPadding)
                .padding(dimensionResource(id = R.dimen.screen_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_large))
        ) {

            // 1. Status Display
            val defaultText = stringResource(R.string.storage_internal_default)
            val currentPath = formatUriForDisplay(state.storageUri, defaultText)
            
            OutlinedTextField(
                value = currentPath,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.storage_label_current)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            // 2. Primary Action: Choose Folder
            Button(
                onClick = { directoryLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.storage_btn_choose),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_small))
                )
            }

            // 3. Secondary Action: Revert to Defaults
            OutlinedButton(
                onClick = { viewModel.updateStorageUri("") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.storage_btn_internal),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_small))
                )
            }
        }
    }
}

private fun formatUriForDisplay(uriString: String, defaultText: String): String {
    if (uriString.isEmpty()) return defaultText

    return try {
        val decodedUri = Uri.decode(uriString)
        // SAF URIs typically look like: content://com.android.externalstorage.documents/tree/primary:drilltutor
        val treePart = decodedUri.substringAfter("/tree/", "")

        if (treePart.isEmpty()) return decodedUri // Fallback if the format is unexpected

        val parts = treePart.split(":", limit = 2)
        if (parts.size == 2) {
            val path = parts[1]
            if (path.isEmpty()) "Root Directory (${parts[0]})" else path
        } else {
            treePart
        }
    } catch (e: Exception) {
        uriString // Fallback to raw string if parsing fails
    }
}