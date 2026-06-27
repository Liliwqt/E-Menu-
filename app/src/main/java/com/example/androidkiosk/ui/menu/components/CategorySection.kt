package com.example.androidkiosk.ui.menu.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme
import com.example.androidkiosk.util.ImageUrlValidator
import java.util.Locale

/** A glassmorphism menu item card showing an image, name, and price. */
@Composable
fun MenuItemCard(
    item: MenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalBackgroundTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardAlpha = if (item.available) 1f else 0.45f

    // Spring-based press scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 800f
        ),
        label = "cardScale"
    )

    GlassCard(
        modifier = modifier
            .aspectRatio(0.75f)   // 3:4 ratio — works well in grids
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = cardAlpha
            }
            .clickable(
                enabled = item.available,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = theme.accentColor
                ),
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageUrlValidator.sanitize(
                    item.imageUrl.ifEmpty { null }
                ) ?: "https://via.placeholder.com/200x150?text=${item.name.replace(" ", "+")}",
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.available) MaterialTheme.colorScheme.onSurface else theme.secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₱${String.format(Locale.getDefault(), "%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.available) theme.accentColor else theme.secondaryTextColor
                )
                if (!item.available) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = theme.secondaryTextColor
                    )
                }
            }
        }
    }
}
