package com.example.androidkiosk.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.example.androidkiosk.ui.theme.LocalReducedMotion

/** Wraps content with a staggered entrance animation. */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Int = 50,
    durationMs: Int = MotionTokens.DurationMedium2,
    slideOffsetY: Float = 40f,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalReducedMotion.current

    if (reducedMotion) {
        Box(modifier = modifier) { content() }
        return
    }

    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(slideOffsetY) }

    LaunchedEffect(index) {
        val delay = index * baseDelayMs
        // Animate alpha
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs,
                delayMillis = delay,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        )
    }

    LaunchedEffect(index) {
        val delay = index * baseDelayMs
        // Animate vertical offset
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = durationMs,
                delayMillis = delay,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value
        }
    ) {
        content()
    }
}

/** Wraps content with a fade-in entrance animation (no slide). */
@Composable
fun FadeInAnimatedItem(
    modifier: Modifier = Modifier,
    durationMs: Int = MotionTokens.DurationMedium1,
    delayMs: Int = 0,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalReducedMotion.current

    if (reducedMotion) {
        Box(modifier = modifier) { content() }
        return
    }

    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs,
                delayMillis = delayMs,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
        }
    ) {
        content()
    }
}
