package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = F1Red,
    secondary = F1White,
    tertiary = F1GreenSec,
    background = F1Black,
    surface = F1DarkGrey,
    onPrimary = F1White,
    onSecondary = F1Black,
    onBackground = F1White,
    onSurface = F1White,
    surfaceVariant = F1MediumGrey,
    onSurfaceVariant = F1LightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = F1Red,
    secondary = F1Black,
    tertiary = F1GreenSec,
    background = F1LightBg,
    surface = F1LightSurface,
    onPrimary = F1White,
    onSecondary = F1White,
    onBackground = F1LightText,
    onSurface = F1LightText,
    surfaceVariant = Color(0xFFEBEBEF),
    onSurfaceVariant = Color(0xFF5E5E6F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep F1 brand colors by default to preserve the premium black/red aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
