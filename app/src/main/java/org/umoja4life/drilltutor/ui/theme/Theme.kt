package org.umoja4life.drilltutor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue900, // Deep Blue
    onPrimary = Color.White,
    secondary = Amber300, // Amber
    surface = Gray050, // Light Gray
    onSurface = BGray900, // Gunmetal dark grey
    error = FgError
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue900, // Retain Deep Blue for header
    onPrimary = Color.White, // Header text remains white
    secondary = Amber300, // Accent remains Amber
    surface = BGray800, // Legacy dark background
    onSurface = Amber050, // Warm off-white for text
    error = FgError
)

@Composable
fun DrillTutorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
