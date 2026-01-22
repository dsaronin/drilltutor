package org.umoja4life.drilltutor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.umoja4life.drilltutor.ui.theme.DrillTutorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.about_screen_title)) },
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
                    navigationIconContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(dimensionResource(id = R.dimen.spacing_medium))
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.about_welcome),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
            Text(
                text = stringResource(id = R.string.about_text_1),
                fontSize = dimensionResource(id = R.dimen.font_size_body_large_about).value.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = dimensionResource(id = R.dimen.line_height_body_large).value.sp
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
            Text(
                text = stringResource(id = R.string.about_text_2),
                fontSize = dimensionResource(id = R.dimen.font_size_body_large_about).value.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = dimensionResource(id = R.dimen.line_height_body_large).value.sp
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
            Text(
                text = stringResource(id = R.string.about_text_3),
                fontSize = dimensionResource(id = R.dimen.font_size_body_large_about).value.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = dimensionResource(id = R.dimen.line_height_body_large).value.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.about_version_format, BuildConfig.VERSION_NAME),
                color = Color(0xFF444444),
                fontWeight = FontWeight.Normal,
                fontSize = dimensionResource(id = R.dimen.font_size_footer).value.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(id = R.string.about_copyright),
                color = Color(0xFF444444),
                fontWeight = FontWeight.Normal,
                fontSize = dimensionResource(id = R.dimen.font_size_footer).value.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    DrillTutorTheme {
        AboutScreen(onNavigateBack = {})
    }
}
