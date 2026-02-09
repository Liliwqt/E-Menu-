@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidkiosk.ui.menu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.androidkiosk.R
import com.example.androidkiosk.model.MenuItem
import java.util.Locale

/**
 * Displays a "Best Sellers" section with an infinite scrolling carousel.
 */
@Composable
fun BestSellersSection(
    items: List<MenuItem>,
    onItemClick: (MenuItem) -> Unit
) {
    if (items.isEmpty()) return

    val factor = 1000
    val infiniteCount = items.size * factor
    val initialPosition = (infiniteCount / 2) - ((infiniteCount / 2) % items.size)

    val state = rememberCarouselState(
        initialItem = initialPosition
    ) { infiniteCount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, top = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.best_sellers),
                    contentDescription = "Best Sellers",
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        HorizontalCenteredHeroCarousel(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 5.dp),
            maxItemWidth = 320.dp,
            itemSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 32.dp),
        ) { index ->
            val item = items[index % items.size]
            BestSellerCarouselItem(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * Visual representation of an item within the Best Sellers carousel.
 */
@Composable
private fun BestSellerCarouselItem(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(5.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.imageUrl.ifEmpty {
                    "https://via.placeholder.com/300x400?text=${item.name.replace(" ", "+")}"
                },
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₱${String.format(Locale.getDefault(), "%.2f", item.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4CAF50)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xFFFFD700), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Best",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
