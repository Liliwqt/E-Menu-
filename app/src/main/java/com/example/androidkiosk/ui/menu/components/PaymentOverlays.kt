package com.example.androidkiosk.ui.menu.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkiosk.R
import com.example.androidkiosk.model.Order
import com.example.androidkiosk.model.PaymentMethod
import com.example.androidkiosk.ui.animation.MotionTokens
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ─────────────────────────────────────────────────────────
// Payment Method Selection Overlay
// ─────────────────────────────────────────────────────────

/**
 * Payment method selection overlay with M3 enhancements.
 *
 * **M3 Enhancements:**
 * - M3 emphasized easing curves for smooth transitions
 * - Slide-in + scale entrance animation
 * - M3 shape tokens and typography
 * - M3 `HorizontalDivider` and `SuggestionChip` components
 * - Press scale animation on payment option cards
 * - Ripple feedback on interactive elements
 */
@Composable
fun PaymentMethodOverlay(
    order: Order,
    onDismiss: () -> Unit,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { isVisible = true }

    fun animatedDismiss() {
        scope.launch {
            isVisible = false
            delay(250)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(0)),
            exit = fadeOut(animationSpec = tween(MotionTokens.DurationMedium1))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    )
            )
        }

        // Content card
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(0)) + slideInVertically(
                initialOffsetY = { it / 8 },
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ),
            exit = fadeOut(tween(MotionTokens.DurationMedium1)) + scaleOut(
                targetScale = 0.1f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedAccelerate)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val paymentConfig = LocalConfiguration.current
                val isPaymentPortrait = paymentConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(if (isPaymentPortrait) 0.92f else 0.55f)
                        .fillMaxHeight(if (isPaymentPortrait) 0.82f else 0.85f)
                        .clickable(enabled = false) { },
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Payment Method",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(onClick = { animatedDismiss() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Order summary bar
                        val payTheme = LocalBackgroundTheme.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order #${order.orderNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₱${String.format(Locale.getDefault(), "%.2f", order.total)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = payTheme.accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "How would you like to pay?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = payTheme.secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Payment options with press scale animation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PaymentOptionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.QrCode2,
                                title = "QR Code Payment",
                                subtitle = "GCash, Maya, BPI, BDO,\nand other QR Ph apps",
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    scope.launch {
                                        isVisible = false
                                        delay(250)
                                        onMethodSelected(PaymentMethod.QR_CODE)
                                    }
                                }
                            )
                            PaymentOptionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Storefront,
                                title = "Pay at Counter",
                                subtitle = "Pay with cash or card\nat the counter",
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                onClick = {
                                    scope.launch {
                                        isVisible = false
                                        delay(250)
                                        onMethodSelected(PaymentMethod.COUNTER)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tap an option to proceed",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = payTheme.secondaryTextColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Payment option card with M3 press scale animation and ripple.
 */
@Composable
private fun PaymentOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Spring-based press scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "paymentCardScale"
    )

    GlassCard(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accentColor),
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.large,
        borderColor = accentColor.copy(alpha = 0.4f),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        accentColor.copy(alpha = 0.1f),
                        MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = LocalBackgroundTheme.current.secondaryTextColor,
                lineHeight = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// QR Code Payment Overlay
// ─────────────────────────────────────────────────────────

/**
 * QR payment overlay with M3 enhancements.
 *
 * **M3 Enhancements:**
 * - M3 emphasized easing curves
 * - M3 `SuggestionChip` for supported apps
 * - M3 shape tokens and typography
 * - Slide-in entrance animation
 */
@Composable
fun QRPaymentOverlay(
    order: Order,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { isVisible = true }

    fun animatedDismiss() {
        scope.launch {
            isVisible = false
            delay(250)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(MotionTokens.DurationMedium1)),
            exit = fadeOut(animationSpec = tween(MotionTokens.DurationMedium1))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    )
            )
        }

        // Content
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(MotionTokens.DurationMedium1, easing = MotionTokens.EasingEmphasizedDecelerate)) + scaleIn(
                initialScale = 0.85f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ),
            exit = fadeOut(tween(MotionTokens.DurationMedium1)) + scaleOut(
                targetScale = 0.85f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedAccelerate)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val qrConfig = LocalConfiguration.current
                val isQrPortrait = qrConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(if (isQrPortrait) 0.92f else 0.55f)
                        .fillMaxHeight(if (isQrPortrait) 0.88f else 0.92f)
                        .clickable(enabled = false) { },
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scan to Pay",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(onClick = { animatedDismiss() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Order number badge with M3 surface container
                        val qrTheme = LocalBackgroundTheme.current
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Order #${order.orderNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = qrTheme.primaryTextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // QR Code Image
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(MaterialTheme.shapes.large)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.shapes.large
                                )
                                .background(Color.White)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.placeholder_qr),
                                contentDescription = "Merchant QR Code",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Scan with any QR Ph-supported app",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = qrTheme.secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Supported apps — M3 SuggestionChips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            listOf("GCash", "Maya", "BPI", "BDO", "UnionBank").forEach { app ->
                                SuggestionChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = app,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 2.dp),
                                    shape = MaterialTheme.shapes.small,
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(color = qrTheme.outlineVariantColor)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Total amount
                        Text(
                            text = "Amount to Pay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = qrTheme.secondaryTextColor
                        )
                        Text(
                            text = "₱${String.format(Locale.getDefault(), "%.2f", order.total)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = qrTheme.accentColor
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Pay at Counter Overlay
// ─────────────────────────────────────────────────────────

/**
 * Counter payment overlay with M3 enhancements.
 *
 * **M3 Enhancements:**
 * - M3 emphasized easing curves
 * - M3 shape tokens and surface containers
 * - Slide-in entrance animation
 */
@Composable
fun CounterPaymentOverlay(
    order: Order,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { isVisible = true }

    fun animatedDismiss() {
        scope.launch {
            isVisible = false
            delay(250)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(MotionTokens.DurationMedium1)),
            exit = fadeOut(animationSpec = tween(MotionTokens.DurationMedium1))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    )
            )
        }

        // Content
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(MotionTokens.DurationMedium1, easing = MotionTokens.EasingEmphasizedDecelerate)) + scaleIn(
                initialScale = 0.85f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ),
            exit = fadeOut(tween(MotionTokens.DurationMedium1)) + scaleOut(
                targetScale = 0.85f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedAccelerate)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val counterConfig = LocalConfiguration.current
                val isCounterPortrait = counterConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(if (isCounterPortrait) 0.92f else 0.55f)
                        .fillMaxHeight(if (isCounterPortrait) 0.82f else 0.85f)
                        .clickable(enabled = false) { },
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pay at Counter",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(onClick = { animatedDismiss() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Counter icon with M3 surface container
                        val counterTheme = LocalBackgroundTheme.current
                        Box(
                            modifier = Modifier
                                .size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = counterTheme.primaryTextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Order Number - prominent display
                        Text(
                            text = "Your Order Number",
                            style = MaterialTheme.typography.bodyLarge,
                            color = counterTheme.secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 40.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "#${order.orderNumber}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = counterTheme.primaryTextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider(color = counterTheme.outlineVariantColor)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Total
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = counterTheme.secondaryTextColor
                        )
                        Text(
                            text = "₱${String.format(Locale.getDefault(), "%.2f", order.total)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = counterTheme.accentColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Please proceed to the counter\nto complete your payment",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = counterTheme.secondaryTextColor,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}
