package org.umoja4life.drilltutor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.umoja4life.drilltutor.R

// Set of Material typography styles to start with
val Typography: Typography
    @Composable
    get() = Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.font_size_display_large).value.sp,
            lineHeight = dimensionResource(id = R.dimen.line_height_display_large).value.sp,
            letterSpacing = dimensionResource(id = R.dimen.letter_spacing_display_large).value.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.font_size_headline_medium).value.sp,
            lineHeight = dimensionResource(id = R.dimen.line_height_headline_medium).value.sp,
            letterSpacing = dimensionResource(id = R.dimen.letter_spacing_zero).value.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.font_size_title_large).value.sp,
            lineHeight = dimensionResource(id = R.dimen.line_height_title_large).value.sp,
            letterSpacing = dimensionResource(id = R.dimen.letter_spacing_zero).value.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = dimensionResource(id = R.dimen.font_size_body_large_default).value.sp,
            lineHeight = dimensionResource(id = R.dimen.line_height_body_large).value.sp,
            letterSpacing = dimensionResource(id = R.dimen.letter_spacing_body_large).value.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = dimensionResource(id = R.dimen.font_size_body_medium_default).value.sp,
            lineHeight = dimensionResource(id = R.dimen.line_height_body_medium).value.sp,
            letterSpacing = dimensionResource(id = R.dimen.letter_spacing_body_medium).value.sp
        )
    )
