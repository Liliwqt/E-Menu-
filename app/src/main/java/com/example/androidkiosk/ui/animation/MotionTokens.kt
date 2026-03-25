package com.example.androidkiosk.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.example.androidkiosk.ui.theme.LocalReducedMotion

/**
 * Material Design 3 motion tokens.
 *
 * Provides standardized duration, easing, and spring specifications
 * following the M3 motion guidelines:
 * https://m3.material.io/styles/motion/overview
 */
@Suppress("unused", "ConstPropertyName")
object MotionTokens {

    // ── Duration Tokens ──────────────────────────────────────────
    /** 50ms — Micro interactions (ripple, state change). */
    const val DurationShort1 = 50

    /** 100ms — Small transitions (icon change, checkbox). */
    const val DurationShort2 = 100

    /** 200ms — Medium transitions (card expand, fade). */
    const val DurationMedium1 = 200

    /** 300ms — Standard transitions (page change, panel). */
    const val DurationMedium2 = 300

    /** 450ms — Large transitions (full-screen, shared element). */
    const val DurationLong1 = 450

    /** 500ms — Extra large transitions (complex shared element). */
    const val DurationLong2 = 500

    // ── Easing Curves ────────────────────────────────────────────

    /**
     * Emphasized easing — the primary M3 easing curve.
     * Used for most transitions. Starts fast, decelerates smoothly.
     */
    val EasingEmphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Emphasized decelerate — for entering elements.
     * Elements arrive quickly and settle into place.
     */
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /**
     * Emphasized accelerate — for exiting elements.
     * Elements accelerate away from the user.
     */
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /**
     * Standard easing — for subtle, utilitarian transitions.
     */
    val EasingStandard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Standard decelerate — for elements entering the screen.
     */
    val EasingStandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

    /**
     * Standard accelerate — for elements leaving the screen.
     */
    val EasingStandardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

    // ── Spring Specifications ────────────────────────────────────

    /** Default spring — balanced feel for most interactions. */
    fun <T> springDefault(): SpringSpec<T> = spring(
        dampingRatio = 0.7f,
        stiffness = 400f
    )

    /** Bouncy spring — playful feel for drag/swipe gestures. */
    fun <T> springBouncy(): SpringSpec<T> = spring(
        dampingRatio = 0.5f,
        stiffness = 300f
    )

    /** Snappy spring — quick, responsive feel for panel toggles. */
    fun <T> springSnappy(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 600f
    )

    /** Gentle spring — soft, slow settle for large elements. */
    fun <T> springGentle(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 200f
    )
}

/**
 * Returns a tween animation spec that respects reduced-motion preferences.
 * When reduced motion is enabled, duration is set to 0 for instant transitions.
 */
@Suppress("unused")
@Composable
fun <T> motionAwareTween(
    durationMillis: Int = MotionTokens.DurationMedium1,
    delayMillis: Int = 0,
    easing: CubicBezierEasing = MotionTokens.EasingEmphasized
) = if (LocalReducedMotion.current) {
    tween(durationMillis = 0, delayMillis = 0)
} else {
    tween<T>(durationMillis = durationMillis, delayMillis = delayMillis, easing = easing)
}

/**
 * Returns a spring animation spec that respects reduced-motion preferences.
 * When reduced motion is enabled, uses very high stiffness for near-instant settle.
 */
@Suppress("unused")
@Composable
fun <T> motionAwareSpring(
    dampingRatio: Float = 0.7f,
    stiffness: Float = 400f
): SpringSpec<T> = if (LocalReducedMotion.current) {
    spring(dampingRatio = 1f, stiffness = 10000f)
} else {
    spring(dampingRatio = dampingRatio, stiffness = stiffness)
}
