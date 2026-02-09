@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidkiosk.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.androidkiosk.R
import com.example.androidkiosk.model.CartItem
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.model.WeatherUiState
import com.example.androidkiosk.ui.menu.components.BestSellersSection
import com.example.androidkiosk.ui.menu.components.CategorySection
import com.example.androidkiosk.ui.menu.components.ErrorScreen
import com.example.androidkiosk.ui.menu.components.ItemDetailOverlay
import com.example.androidkiosk.ui.menu.components.LoadingScreen
import com.example.androidkiosk.ui.menu.components.WeatherSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Main entry point for the Menu Screen.
 *
 * Displays the weather widget, best-sellers carousel, and category rows.
 * Item detail, cart, and checkout are shown as overlays.
 */
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onNavigateToCart: () -> Unit = {}
) {
    val bestSellers by viewModel.bestSellers.collectAsState()
    val weatherState by viewModel.weather.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var showCart by remember { mutableStateOf(false) }
    var showCheckout by remember { mutableStateOf(false) }
    
    val blurAmount by animateFloatAsState(
        targetValue = if (selectedItem != null || showCart || showCheckout) 10f else 0f,
        animationSpec = tween(300),
        label = "blur"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content with optional blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (blurAmount > 0f) Modifier.blur(blurAmount.dp) else Modifier)
        ) {
            Image(
                painter = painterResource(id = R.drawable.menu_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Scaffold(
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize(),
            ) { paddingValues ->
                when {
                    isLoading -> LoadingScreen()
                    errorMessage != null -> {
                        ErrorScreen(
                            message = errorMessage ?: "Unknown error",
                            onRetry = { viewModel.retryLoading() }
                        )
                    }
                    else -> {
                        MenuContent(
                            modifier = Modifier.padding(paddingValues),
                            bestSellers = bestSellers,
                            categories = categories,
                            weatherState = weatherState,
                            onItemClick = { item -> selectedItem = item }
                        )
                    }
                }
            }
        }

        // Cart FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        ) {
            LargeFloatingActionButton(
                onClick = { showCart = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(
                    bottomEnd = 12.dp,
                    bottomStart = 40.dp,
                    topEnd = 40.dp,
                    topStart = 35.dp
                )
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "View Cart",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            if (cartItems.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                        .background(Color.Red, CircleShape)
                        .size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cartItems.sumOf { it.quantity }.toString(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Item detail overlay
        if (selectedItem != null) {
            ItemDetailOverlay(
                item = selectedItem!!,
                onDismiss = { selectedItem = null },
                onAddToCart = { menuItem ->
                    viewModel.addToCart(menuItem)
                    selectedItem = null
                }
            )
        }
        
        // Cart overlay
        if (showCart) {
            CartOverlay(
                viewModel = viewModel,
                onDismiss = { showCart = false },
                onProceedToCheckout = {
                    showCart = false
                    showCheckout = true
                }
            )
        }
        
        // Checkout overlay
        if (showCheckout) {
            CheckoutOverlay(
                viewModel = viewModel,
                onDismiss = { showCheckout = false },
                onOrderConfirmed = { showCheckout = false }
            )
        }
    }
}

/**
 * Renders the main scrollable content of the menu.
 */
@Composable
private fun MenuContent(
    modifier: Modifier = Modifier,
    bestSellers: List<MenuItem>,
    categories: List<CategoryWithItems>,
    weatherState: WeatherUiState,
    onItemClick: (MenuItem) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 0.dp)
    ) {
        item {
            WeatherSection(weatherState)
        }

        if (bestSellers.isNotEmpty()) {
            item {
                BestSellersSection(
                    items = bestSellers,
                    onItemClick = onItemClick
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        val otherCategories = categories.filter { it.categoryName != "Best Sellers" }
        items(otherCategories) { category ->
            CategorySection(
                category = category,
                onItemClick = onItemClick
            )
        }
    }
}


/**
 * Cart overlay with animated entry/exit
 */
@Composable
private fun CartOverlay(
    viewModel: MenuViewModel,
    onDismiss: () -> Unit,
    onProceedToCheckout: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var isCheckingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { isVisible = true }

    fun animatedDismiss() {
        scope.launch {
            isVisible = false
            delay(500)
            onDismiss()
        }
    }

    fun proceedToCheckout() {
        isCheckingOut = true
        scope.launch {
            isVisible = false
            delay(500)
            onProceedToCheckout()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(400))
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
                initialScale = 0.1f,
                transformOrigin = TransformOrigin(1f, 1f),
                animationSpec = tween(500)
            ),
            exit = fadeOut(tween(500)) + scaleOut(
                targetScale = 0.1f,
                transformOrigin = TransformOrigin(1f, 1f),
                animationSpec = tween(500)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .size(450.dp, 650.dp)
                        .clickable(enabled = false) { },
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Cart",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(onClick = { animatedDismiss() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Items List
                        if (cartItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Your cart is empty", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(cartItems) { item ->
                                    CartListItem(
                                        cartItem = item,
                                        onRemove = { viewModel.removeFromCart(it) },
                                        onUpdateQuantity = { cartItem, qty ->
                                            viewModel.updateQuantity(cartItem, qty)
                                        }
                                    )
                                }
                            }
                        }

                        // Footer / Summary
                        val total = cartItems.sumOf { it.menuItem.price * it.quantity }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "₱${String.format(Locale.getDefault(), "%.2f", total)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { proceedToCheckout() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = cartItems.isNotEmpty()
                            ) {
                                Text("PROCEED TO CHECKOUT", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartListItem(
    cartItem: CartItem,
    onRemove: (CartItem) -> Unit,
    onUpdateQuantity: (CartItem, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cartItem.menuItem.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.menuItem.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                "₱${String.format(Locale.getDefault(), "%.2f", cartItem.menuItem.price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onUpdateQuantity(cartItem, cartItem.quantity - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "${cartItem.quantity}",
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onUpdateQuantity(cartItem, cartItem.quantity + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = { onRemove(cartItem) }) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f))
        }
    }
}

/**
 * Checkout overlay that displays order summary and confirmation
 */
@Composable
private fun CheckoutOverlay(
    viewModel: MenuViewModel,
    onDismiss: () -> Unit,
    onOrderConfirmed: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .size(450.dp, 650.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(0.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Order Summary
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.menuItem.name} x${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "₱${String.format(Locale.getDefault(), "%.2f", item.menuItem.price * item.quantity)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Footer / Total and Confirm
                val total = cartItems.sumOf { it.menuItem.price * it.quantity }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Amount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "₱${String.format(Locale.getDefault(), "%.2f", total)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.clearCart()
                            onOrderConfirmed()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONFIRM ORDER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
