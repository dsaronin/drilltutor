package org.umoja4life.drilltutor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LessonsView(
    modifier: Modifier = Modifier
) {
    // --- TEST HARNESS DATA ---
    val testTitle = stringResource(id = R.string.huge_text)
    val testDescription = listOf(
        stringResource(id = R.string.large_text),
        stringResource(id = R.string.large_text)
    )
    val testNotes = List(5) {
        FlashcardData(
            front = stringResource(id = R.string.normal_text),
            back = stringResource(id = R.string.normal_text)
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(dimensionResource(id = R.dimen.spacing_medium))
    ) {
        LessonTitle(title = testTitle)

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))

        LessonDescription(lines = testDescription)

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

        LessonNotes(notes = testNotes)
    }
}

@Composable
private fun LessonTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun LessonDescription(lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_medium))) {
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LessonNotes(notes: List<FlashcardData>) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small))) {
        notes.forEach { card ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.bullet_char),
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                // Using a Row for the Front/Back split
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = card.front,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Simulated Tab
                    Text(
                        text = card.back,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}