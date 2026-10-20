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
    primary = EmergencyRedBright,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedDark,
    onPrimaryContainer = Color.White,
    secondary = DelhiSecondary,
    onSecondary = Color.White,
    tertiary = StatusAmber,
    background = EmergencyDarkBg,
    surface = EmergencyDarkSurface,
    surfaceVariant = EmergencyDarkSurfaceVariant,
    onBackground = EmergencyDarkText,
    onSurface = EmergencyDarkText,
    onSurfaceVariant = EmergencyDarkSubtext,
    outline = EmergencyDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DelhiPrimary,
    onPrimary = Color.White,
    primaryContainer = DelhiSecondary,
    onPrimaryContainer = Color.White,
    secondary = EmergencyRed,
    onSecondary = Color.White,
    tertiary = DelhiTertiary,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = LightSubtext,
    outline = LightBorder
)

@Composable
fun SahayakTheme(
    emergencyDarkMode: Boolean = false,
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        emergencyDarkMode -> DarkColorScheme
        systemDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
