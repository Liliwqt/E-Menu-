package com.example.androidkiosk.ui.menu.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.ui.animation.MotionTokens
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme
import com.example.androidkiosk.util.ImageUrlValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/** Item detail overlay with M3 components and enhanced animations. */
@Composable
fun ItemDetailOverlay(
    item: MenuItem,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem, Int) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var isAddingToCart by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { isVisible = true }

    fun animatedDismiss() {
        scope.launch {
            isVisible = false
            delay(150)
            onDismiss()
        }
    }

    fun animatedAddToCart() {
        scope.launch {
            isAddingToCart = true
            isVisible = false
            delay(250)
            onAddToCart(item, quantity)
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
            enter = fadeIn(tween(MotionTokens.DurationMedium1, easing = MotionTokens.EasingStandard)),
            exit = fadeOut(tween(if (isAddingToCart) MotionTokens.DurationMedium1 else MotionTokens.DurationShort2))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { animatedDismiss() }
                    )
            )
        }

        // Content card with container transform-style animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                tween(MotionTokens.DurationMedium1, easing = MotionTokens.EasingEmphasizedDecelerate)
            ) + scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ) + slideInVertically(
                initialOffsetY = { it / 12 },
                animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EasingEmphasizedDecelerate)
            ),
            exit = if (isAddingToCart) {
                fadeOut(tween(MotionTokens.DurationMedium1)) + scaleOut(
                    targetScale = 0.1f,
                    transformOrigin = TransformOrigin(1f, 1f),
                    animationSpec = tween(MotionTokens.DurationMedium1, easing = MotionTokens.EasingEmphasizedAccelerate)
                )
            } else {
                fadeOut(tween(MotionTokens.DurationShort2)) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(MotionTokens.DurationShort2, easing = MotionTokens.EasingEmphasizedAccelerate)
                ) + slideOutVertically(
                    targetOffsetY = { it / 12 },
                    animationSpec = tween(MotionTokens.DurationShort2, easing = MotionTokens.EasingEmphasizedAccelerate)
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val detailConfig = LocalConfiguration.current
                val isDetailPortrait = detailConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(if (isDetailPortrait) 0.92f else 0.5f)
                        .fillMaxHeight(if (isDetailPortrait) 0.60f else 0.9f)
                        .clickable(enabled = false) { },
                    shape = MaterialTheme.shapes.extraLarge,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Image section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                        ) {
                            AsyncImage(
                                model = ImageUrlValidator.sanitize(
                                    item.imageUrl.ifEmpty { null }
                                ) ?: "https://via.placeholder.com/400x300?text=${item.name.replace(" ", "+")}",
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Close button — M3 FilledTonalIconButton
                            FilledTonalIconButton(
                                onClick = { animatedDismiss() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color.Black.copy(alpha = 0.5f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }

                        // Detail section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            val detailTheme = LocalBackgroundTheme.current
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "₱${String.format(Locale.getDefault(), "%.2f", item.price)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = detailTheme.accentColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // M3 HorizontalDivider
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = detailTheme.outlineVariantColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Item ID: ${item.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = detailTheme.secondaryTextColor
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Tap outside to close",
                                style = MaterialTheme.typography.bodySmall,
                                color = detailTheme.secondaryTextColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            // Quantity selector + Add to Cart
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Quantity selector with M3 FilledTonalIconButtons
                                Row(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.large)
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalIconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        enabled = item.available && quantity > 1,
                                        modifier = Modifier.size(40.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = detailTheme.primaryTextColor,
                                            disabledContentColor = detailTheme.secondaryTextColor
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease quantity"
                                        )
                                    }
                                    Text(
                                        text = "$quantity",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = detailTheme.primaryTextColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    FilledTonalIconButton(
                                        onClick = { if (quantity < 99) quantity++ },
                                        enabled = item.available && quantity < 99,
                                        modifier = Modifier.size(40.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = detailTheme.primaryTextColor,
                                            disabledContentColor = detailTheme.secondaryTextColor
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase quantity"
                                        )
                                    }
                                }

                                // Add to Cart button — M3 FilledButton
                                Button(
                                    onClick = { animatedAddToCart() },
                                    modifier = Modifier
                                        .height(48.dp),
                                    shape = MaterialTheme.shapes.large,
                                    enabled = item.available,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Add to Cart",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
