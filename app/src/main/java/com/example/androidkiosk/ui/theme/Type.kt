package com.example.androidkiosk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ====================================================
 * Typography (Font Styles) for the App
 * ====================================================
 * 
 * Material3 typography defines text styles used throughout the app.
 * These are accessed via MaterialTheme.typography.bodyLarge, etc.
 * 
 * For Beginners:
 * - displayLarge/Medium/Small: Big headlines
 * - headlineLarge/Medium/Small: Section headers
 * - titleLarge/Medium/Small: Item titles
 * - bodyLarge/Medium/Small: Regular text
 * - labelLarge/Medium/Small: Buttons, captions
 */

val Typography = Typography(
    // Large body text (default for paragraphs)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Medium title (for cards, list items)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    
    // Large title (for section headers)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // Large label (for buttons)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    
    /* You can add more custom styles:
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    */
)
