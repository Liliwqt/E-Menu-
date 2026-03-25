package com.example.androidkiosk.ui.menu.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidkiosk.ui.menu.UIMode
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme

/**
 * Full-screen initial chooser that appears before any menu content.
 * The user must pick either Vertical or Horizontal menu mode to proceed.
 *
 * Uses glassmorphism cards with spring-based press animations,
 * staggered entrance animations, and theme-aware colors.
 */
@Composable
fun MenuModeSelectionScreen(
    onModeSelected: (UIMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalBackgroundTheme.current

    // Staggered entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            // Title
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(500)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose your menu layout",
                        style = MaterialTheme.typography.titleMedium,
                        color = theme.secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Mode cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Vertical menu card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 150)) + scaleIn(
                        initialScale = 0.85f,
                        animationSpec = tween(500, delayMillis = 150)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    ModeCard(
                        icon = Icons.AutoMirrored.Filled.ViewList,
                        title = "Vertical Menu",
                        description = "Classic side panel navigation\nwith swipeable category pages",
                        onClick = { onModeSelected(UIMode.CURRENT) }
                    )
                }

                // Horizontal menu card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 300)) + scaleIn(
                        initialScale = 0.85f,
                        animationSpec = tween(500, delayMillis = 300)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    ModeCard(
                        icon = Icons.Default.ViewCarousel,
                        title = "Horizontal Menu",
                        description = "Smooth scrolling layout\nwith all categories in a row",
                        onClick = { onModeSelected(UIMode.NEW_HORIZONTAL) }
                    )
                }

                // Portrait menu card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 450)) + scaleIn(
                        initialScale = 0.85f,
                        animationSpec = tween(500, delayMillis = 450)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    ModeCard(
                        icon = Icons.Default.ViewColumn,
                        title = "Portrait Menu",
                        description = "Top category bar with\nsmooth vertical scrolling",
                        onClick = { onModeSelected(UIMode.PORTRAIT) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
private fun MenuModeSelectionPreview() {
    MaterialTheme {
        MenuModeSelectionScreen(onModeSelected = {})
    }
}

/**
 * A glass card representing a single mode choice.
 * Features a spring-based press-scale animation and ripple feedback.
 */
@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalBackgroundTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "modeCardScale"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = theme.accentColor),
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = 4.dp,
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = theme.accentColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = theme.primaryTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.secondaryTextColor,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}
