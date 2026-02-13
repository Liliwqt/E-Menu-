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

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
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
                    navController.popBackStack(Route.Menu, inclusive = false)
                }
            )
        }
    }
}
