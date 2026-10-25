package com.ndaily.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.R

val PromptFamily = FontFamily(
    Font(R.font.prompt_regular, FontWeight.Normal),
    Font(R.font.prompt_medium, FontWeight.Medium),
    Font(R.font.prompt_semibold, FontWeight.SemiBold),
    Font(R.font.prompt_bold, FontWeight.Bold),
)

val DailyTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = PromptFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Foreground,
    ),
    titleLarge = TextStyle(
        fontFamily = PromptFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Ink,
    ),
)
