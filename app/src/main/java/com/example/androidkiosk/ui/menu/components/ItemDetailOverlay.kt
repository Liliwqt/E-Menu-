package com.example.androidkiosk.ui.menu.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.androidkiosk.model.MenuItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ItemDetailOverlay(
    item: MenuItem,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var isAddingToCart by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { isVisible = true }

    fun animatedDismiss() {
        scope.launch {
            isVisible = false
            delay(400)
            onDismiss()
        }
    }

    fun animatedAddToCart() {
        scope.launch {
            isAddingToCart = true
            isVisible = false
            delay(500)
            onAddToCart(item)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(if (isAddingToCart) 500 else 400))
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

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ),
            exit = if (isAddingToCart) {
                fadeOut(tween(500)) + scaleOut(
                    targetScale = 0.1f,
                    transformOrigin = TransformOrigin(1f, 1f),
                    animationSpec = tween(400)
                )
            } else {
                fadeOut(tween(300)) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(300)
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .size(400.dp, 600.dp)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            AsyncImage(
                                model = item.imageUrl.ifEmpty {
                                    "https://via.placeholder.com/400x300?text=${item.name.replace(" ", "+")}"
                                },
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { animatedDismiss() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }

                            if (item.available) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color(0xFF4CAF50), RoundedCornerShape(10.dp))
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = "Available",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color(0xE8414141), RoundedCornerShape(10.dp))
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = "Out of Stock",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
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
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Item ID: ${item.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Tap outside to close",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { animatedAddToCart() },
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(48.dp)
                                        .align(Alignment.BottomEnd),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = item.available,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                ) {
                                    Text(
                                        text = "Add to Cart",
                                        fontSize = 11.sp,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
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
