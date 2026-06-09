package com.example.androidkiosk.ui.menu.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkiosk.ui.animation.MotionTokens
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme

/** Maps a category name to a recognizable Material icon. */
fun categoryIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        "best" in lower && "seller" in lower -> Icons.Default.Star
        "beverage" in lower || "drink" in lower -> Icons.Default.LocalCafe
        "coffee" in lower -> Icons.Default.Coffee
        "tea" in lower -> Icons.Default.LocalCafe
        "juice" in lower || "smoothie" in lower -> Icons.Default.LocalBar
        "food" in lower || "meal" in lower || "main" in lower || "entree" in lower -> Icons.Default.Restaurant
        "dessert" in lower || "sweet" in lower -> Icons.Default.Cake
        "snack" in lower -> Icons.Default.Fastfood
        "pizza" in lower -> Icons.Default.LocalPizza
        "pasta" in lower || "noodle" in lower -> Icons.Default.RamenDining
        "burger" in lower || "sandwich" in lower -> Icons.Default.LunchDining
        "ice" in lower && "cream" in lower -> Icons.Default.Icecream
        "dinner" in lower -> Icons.Default.DinnerDining
        "breakfast" in lower -> Icons.Default.Restaurant
        else -> Icons.Default.Category
    }
}

/** A collapsible glassmorphism side panel showing category icons, */
@Composable
fun SideCategoryPanel(
    categories: List<String>,
    selectedIndex: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Spring-based panel width animation for natural feel
    val panelWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 64.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 600f
        ),
        label = "panelWidth"
    )

    // Animated toggle icon rotation
    val toggleRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "toggleRotation"
    )

    val panelTheme = LocalBackgroundTheme.current

    GlassCard(
        modifier = modifier
            .width(panelWidth)
            .fillMaxHeight()
            .clipToBounds()
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp),
        shape = MaterialTheme.shapes.large,
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .clipToBounds()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toggle / hamburger icon at the top with ripple
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(
                        indication = ripple(
                            bounded = true,
                            color = panelTheme.accentColor
                        ),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onToggleExpand
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = panelTheme.accentColor,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationY = toggleRotation }
                    )
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandHorizontally(
                            animationSpec = tween(MotionTokens.DurationMedium2),
                            expandFrom = Alignment.Start
                        ) + fadeIn(tween(MotionTokens.DurationMedium1)),
                        exit = shrinkHorizontally(
                            animationSpec = tween(MotionTokens.DurationMedium1),
                            shrinkTowards = Alignment.Start
                        ) + fadeOut(tween(MotionTokens.DurationShort2))
                    ) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Menu",
                                color = panelTheme.accentColor,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // M3 HorizontalDivider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 10.dp),
                thickness = 1.dp,
                color = panelTheme.outlineVariantColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category items with M3 NavigationRail-style indicator
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                categories.forEachIndexed { index, name ->
                    val isSelected = index == selectedIndex

                    // Animated background color
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) panelTheme.categorySelectedColor.copy(alpha = 0.18f) else Color.Transparent,
                        animationSpec = tween(MotionTokens.DurationMedium1),
                        label = "catBg$index"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) panelTheme.categorySelectedColor else panelTheme.categoryUnselectedColor,
                        animationSpec = tween(MotionTokens.DurationMedium1),
                        label = "catTint$index"
                    )

                    // Animated selection indicator width (M3 NavigationRail style)
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 3.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = 500f
                        ),
                        label = "indicator$index"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(bgColor)
                            // Draw M3-style vertical indicator bar on the left
                            .drawBehind {
                                if (indicatorWidth.toPx() > 0f) {
                                    drawRoundRect(
                                        color = panelTheme.categorySelectedColor,
                                        topLeft = Offset(0f, size.height * 0.15f),
                                        size = Size(
                                            indicatorWidth.toPx(),
                                            size.height * 0.7f
                                        ),
                                        cornerRadius = CornerRadius(
                                            indicatorWidth.toPx() / 2f,
                                            indicatorWidth.toPx() / 2f
                                        )
                                    )
                                }
                            }
                            .clickable(
                                indication = ripple(
                                    bounded = true,
                                    color = panelTheme.categorySelectedColor
                                ),
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            ) {
                                onCategoryClick(index)
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon(name),
                                contentDescription = name,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandHorizontally(
                                    animationSpec = tween(MotionTokens.DurationMedium2),
                                    expandFrom = Alignment.Start
                                ) + fadeIn(tween(MotionTokens.DurationMedium1)),
                                exit = shrinkHorizontally(
                                    animationSpec = tween(MotionTokens.DurationMedium1),
                                    shrinkTowards = Alignment.Start
                                ) + fadeOut(tween(MotionTokens.DurationShort2))
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        color = iconTint,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
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
