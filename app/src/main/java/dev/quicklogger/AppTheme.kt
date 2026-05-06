package dev.quicklogger

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Composable
fun QuickLoggerTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val darkTheme = shouldUseDarkTheme(themeMode)
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFF4F4F5),
            onPrimary = Color(0xFF18181B),
            secondary = Color(0xFFB4B4BC),
            surface = Color(0xFF303034),
            onSurface = Color(0xFFF4F4F5),
            background = Color(0xFF0B0B0D),
            onBackground = Color(0xFFF4F4F5),
            inversePrimary = Color(0xFF18181B),
            inverseSurface = Color(0xFFF4F4F5),
            inverseOnSurface = Color(0xFF18181B),
            outline = Color(0xFF6F6F78),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF18181B),
            onPrimary = Color.White,
            secondary = Color(0xFF52525B),
            surface = Color.White,
            onSurface = Color(0xFF18181B),
            background = Color(0xFFE9EAEC),
            onBackground = Color(0xFF18181B),
            inversePrimary = Color(0xFFF4F4F5),
            inverseSurface = Color(0xFF18181B),
            inverseOnSurface = Color(0xFFF4F4F5),
            outline = Color(0xFFB8BAC0),
        )
    }
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif),
        ),
        content = content,
    )
}

@Composable
fun shouldUseDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

fun appBackground(darkTheme: Boolean): Brush =
    if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0B0B0D), Color(0xFF161619)),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF1F2F4), Color(0xFFE2E3E6)),
        )
    }
