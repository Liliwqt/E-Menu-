package com.example.androidkiosk.ui.menu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme

/** Accent color for prices and highlights on glass surfaces (legacy fallback). */
@Suppress("unused")
val GlassAccent = Color(0xFF4FC3F7)

/** A glassmorphism-styled card with a translucent, tinted appearance. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    backgroundColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 0.dp,
    useTonalSurface: Boolean = false,
    content: @Composable () -> Unit
) {
    val theme = LocalBackgroundTheme.current
    val resolvedContentColor = if (contentColor != Color.Unspecified) contentColor else theme.primaryTextColor

    // ── Border: thin, subtle accent-tinted edge ──────────────────────
    val borderModifier = when {
        borderColor != Color.Unspecified -> Modifier.border(borderWidth, borderColor, shape)
        borderBrush != null -> Modifier.border(borderWidth, borderBrush, shape)
        else -> Modifier.border(
            borderWidth,
            Brush.linearGradient(
                colors = listOf(
                    theme.accentColor.copy(alpha = 0.35f),
                    theme.accentColor.copy(alpha = 0.08f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape
        )
    }

    // ── Background: translucent tint + gradient overlay ──────────────
    val tintColor = when {
        backgroundColor != Color.Unspecified -> backgroundColor
        useTonalSurface -> theme.surfaceContainerHigh
        else -> theme.surfaceColor.copy(alpha = 0.35f)
    }

    val gradientOverlay = if (backgroundColor == Color.Unspecified && !useTonalSurface) {
        Brush.linearGradient(
            colors = listOf(
                theme.glassGradientStart,
                theme.glassGradientEnd
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else null

    // ── Optional M3 shadow elevation ─────────────────────────────────
    val elevationModifier = if (elevation > 0.dp) {
        Modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = theme.surfaceContainerLowest.copy(alpha = 0.5f),
            spotColor = theme.surfaceContainerLowest.copy(alpha = 0.5f)
        )
    } else {
        Modifier
    }

    CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
        Box(
            modifier = modifier
                .then(elevationModifier)
                .clip(shape)
                .background(tintColor)
                .then(
                    if (gradientOverlay != null) Modifier.background(gradientOverlay)
                    else Modifier
                )
                .then(borderModifier)
        ) {
            content()
        }
    }
}
