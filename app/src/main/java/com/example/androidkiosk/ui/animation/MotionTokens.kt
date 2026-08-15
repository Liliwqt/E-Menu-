package com.example.androidkiosk.ui.animation

import androidx.compose.animation.core.CubicBezierEasing

/** Material Design 3 motion tokens. */
@Suppress("ConstPropertyName")
object MotionTokens {

    // ── Duration Tokens ──────────────────────────────────────────
    /** 100ms — Small transitions (icon change, checkbox). */
    const val DurationShort2 = 100

    /** 200ms — Medium transitions (card expand, fade). */
    const val DurationMedium1 = 200

    /** 300ms — Standard transitions (page change, panel). */
    const val DurationMedium2 = 300

    // ── Easing Curves ────────────────────────────────────────────

    /** Emphasized decelerate — for entering elements. */
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /** Emphasized accelerate — for exiting elements. */
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /** Standard easing — for subtle, utilitarian transitions. */
    val EasingStandard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

}
