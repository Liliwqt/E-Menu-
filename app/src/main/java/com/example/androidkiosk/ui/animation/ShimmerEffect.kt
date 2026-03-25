package com.example.androidkiosk.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme
import com.example.androidkiosk.ui.theme.LocalReducedMotion

/**
 * Modifier extension that applies a shimmer loading effect.
 *
 * Creates an animated diagonal gradient sweep that simulates
 * content loading. Respects reduced-motion preferences by
 * showing a static placeholder when reduced motion is enabled.
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val theme = LocalBackgroundTheme.current
    val reducedMotion = LocalReducedMotion.current

    return if (reducedMotion) {
        this.background(
            theme.surfaceContainerHigh,
            MaterialTheme.shapes.medium
        )
    } else {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )

        val shimmerColors = listOf(
            theme.surfaceContainerLow,
            theme.surfaceContainerHigh,
            theme.surfaceContainerLow
        )

        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - 200f, translateAnim - 200f),
            end = Offset(translateAnim, translateAnim)
        )

        this.background(brush, MaterialTheme.shapes.medium)
    }
}

/**
 * Shimmer placeholder matching [MenuItemCard] dimensions.
 * Shows a skeleton card with image area and text lines.
 */
@Composable
fun ShimmerMenuItemCard(
    modifier: Modifier = Modifier
) {
    val theme = LocalBackgroundTheme.current

    Box(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(MaterialTheme.shapes.medium)
            .shimmerEffect()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(theme.surfaceContainerHigh.copy(alpha = 0.5f))
            )
            // Text placeholder area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Title line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surfaceContainerHighest.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Price line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surfaceContainerHighest.copy(alpha = 0.4f))
                )
            }
        }
    }
}

/**
 * Shimmer placeholder for a category header.
 */
@Composable
fun ShimmerCategoryHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .shimmerEffect()
    )
}

/**
 * Full shimmer skeleton layout matching the menu content structure.
 * Shows a side panel skeleton + category header + 3-column grid of card skeletons.
 */
@Composable
fun ShimmerMenuSkeleton(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Side panel skeleton
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .padding(start = 6.dp, top = 6.dp, bottom = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )

        // Content area skeleton
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Category header skeleton
            ShimmerCategoryHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Separator skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(1.dp)
                    .background(
                        LocalBackgroundTheme.current.surfaceOverlayColor
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid skeleton — 3 columns, 2 rows
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(6) {
                    ShimmerMenuItemCard(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
