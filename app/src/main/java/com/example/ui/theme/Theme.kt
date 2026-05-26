package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EduCoreColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    secondary = EmeraldSecondary,
    tertiary = AmberWarning,
    background = DarkBg,
    surface = DarkCardBg,
    error = RedDanger,
    onPrimary = HeadingText,
    onSecondary = DarkBg,
    onTertiary = DarkBg,
    onBackground = HeadingText,
    onSurface = HeadingText,
    onSurfaceVariant = BodyText,
    outline = CardBorderColor
)

@Composable
fun EduCoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EduCoreColorScheme,
        typography = Typography,
        content = content
    )
}
