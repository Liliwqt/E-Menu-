package com.example.androidkiosk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.androidkiosk.ui.cart.CartScreen
import com.example.androidkiosk.ui.checkout.CheckoutScreen
import com.example.androidkiosk.ui.menu.MenuScreen
import com.example.androidkiosk.ui.menu.MenuViewModel

/**
 * Main navigation graph for the app.
 *
 * The [MenuViewModel] is scoped to the NavHost so state (especially the cart)
 * is shared across all screens without needing a global store.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Share the ViewModel across the nav graph so the cart persists
    val menuViewModel: MenuViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Route.Menu,
        modifier = modifier
    ) {
        composable<Route.Menu> {
            MenuScreen(
                viewModel = menuViewModel,
                onNavigateToCart = { navController.navigate(Route.Cart) }
            )
        }

        composable<Route.Cart> {
            CartScreen(
                viewModel = menuViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCheckout = {
                    navController.navigate(Route.Checkout)
                }
            )
        }

        composable<Route.Checkout> {
            CheckoutScreen(
                viewModel = menuViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderConfirmed = {
                    // Pop back to the menu screen after order is confirmed
                    navController.popBackStack(Route.Menu, inclusive = false)
                }
            )
        }
    }
}
