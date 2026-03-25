package com.example.androidkiosk.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 shape token system.
 *
 * Defines corner radii for all M3 shape categories:
 * - **Extra Small** (4dp): Chips, small badges, tooltips
 * - **Small** (8dp): Buttons, text fields, snackbars
 * - **Medium** (12dp): Cards, dialogs, small FABs
 * - **Large** (16dp): Large cards, navigation drawers
 * - **Extra Large** (28dp): Bottom sheets, large FABs, full-screen dialogs
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
