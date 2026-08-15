package com.example.androidkiosk.ui.menu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.ui.animation.FadeInAnimatedItem
import com.example.androidkiosk.ui.animation.StaggeredAnimatedItem
import com.example.androidkiosk.ui.theme.LocalBackgroundTheme

/** A full-page category section displayed inside a [androidx.compose.foundation.pager.HorizontalPager] page. */
@Composable
fun CategoryPageContent(
    category: CategoryWithItems,
    onItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        val catTheme = LocalBackgroundTheme.current

        // ── Category header with icon ──────────────────────────────
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = MaterialTheme.shapes.medium,
            borderWidth = 1.dp,
            elevation = 1.dp,
            useTonalSurface = true
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = categoryIcon(category.categoryName),
                        contentDescription = null,
                        tint = catTheme.accentColor,
                        modifier = Modifier.height(24.dp)
                    )
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = catTheme.primaryTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── M3 HorizontalDivider ────────────────────────────────────
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 4.dp),
            thickness = 1.dp,
            color = catTheme.outlineVariantColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── 3-column grid of menu items with staggered animations ──
        if (category.items.isEmpty()) {
            FadeInAnimatedItem {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items available",
                        color = catTheme.secondaryTextColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = category.items,
                    key = { _, item -> item.id }
                ) { index, item ->
                    StaggeredAnimatedItem(
                        index = index,
                        baseDelayMs = 40,
                        slideOffsetY = 30f
                    ) {
                        MenuItemCard(
                            item = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}
