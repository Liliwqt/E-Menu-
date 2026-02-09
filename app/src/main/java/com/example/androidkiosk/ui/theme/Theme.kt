package com.example.androidkiosk.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * ====================================================
 * App Theme Configuration
 * ====================================================
 * 
 * This file defines the overall look of the app.
 * Material3 themes include colors, typography, and shapes.
 * 
 * For Beginners:
 * - Dark/Light themes change based on system settings
 * - Dynamic color (Android 12+) uses wallpaper colors
 * - MaterialTheme provides these values to all child composables
 */

// Colors for dark mode
private val DarkColorScheme = darkColorScheme(
    primary = Black120,
    secondary = Black120,
    tertiary = Pink80
)

// Colors for light mode
private val LightColorScheme = lightColorScheme(
    primary = Black120,
    secondary = Black120,
    tertiary = Black120
    
    /* You can override more default colors here:
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Main theme composable that wraps your app
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic colors from wallpaper (Android 12+)
 * @param content The composable content to display with this theme
 */
@Composable
fun AndroidKioskTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Determine which color scheme to use
    val colorScheme = when {
        // Use dynamic colors if available and enabled
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Fall back to static color schemes
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Apply the theme to all child composables
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
