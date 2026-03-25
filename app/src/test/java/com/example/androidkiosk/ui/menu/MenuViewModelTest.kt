package com.example.androidkiosk.ui.menu

import app.cash.turbine.test
import com.example.androidkiosk.domain.repository.AppSettingsRepository
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.domain.repository.OrderRepository
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var menuRepository: MenuRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var appSettingsRepository: AppSettingsRepository
    private lateinit var viewModel: MenuViewModel

    private val sampleItems = listOf(
        MenuItem(id = "1", name = "Chicken Burger", price = 129.0),
        MenuItem(id = "2", name = "Beef Burger", price = 149.0)
    )

    private val sampleCategories = listOf(
        CategoryWithItems(categoryName = "Burgers", items = sampleItems)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        menuRepository = mockk(relaxed = true)
        orderRepository = mockk(relaxed = true)
        appSettingsRepository = mockk(relaxed = true)

        every { menuRepository.observeCategories() } returns flowOf(sampleCategories)
        every { menuRepository.observeBestSellers() } returns flowOf(sampleItems)

        viewModel = MenuViewModel(menuRepository, orderRepository, appSettingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addToCart adds new item with quantity 1`() = runTest {
        val item = sampleItems[0]
        viewModel.addToCart(item)

        viewModel.cartItems.test {
            val cart = awaitItem()
            assertEquals(1, cart.size)
            assertEquals(item.id, cart[0].menuItem.id)
            assertEquals(1, cart[0].quantity)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `addToCart increments quantity for existing item`() = runTest {
        val item = sampleItems[0]
        viewModel.addToCart(item)
        viewModel.addToCart(item)

        viewModel.cartItems.test {
            val cart = awaitItem()
            assertEquals(1, cart.size)
            assertEquals(2, cart[0].quantity)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `removeFromCart removes the item`() = runTest {
        val item = sampleItems[0]
        viewModel.addToCart(item)
        val cartItem = viewModel.cartItems.value[0]
        viewModel.removeFromCart(cartItem)

        viewModel.cartItems.test {
            val cart = awaitItem()
            assertTrue(cart.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `updateQuantity sets new quantity`() = runTest {
        val item = sampleItems[0]
        viewModel.addToCart(item)
        val cartItem = viewModel.cartItems.value[0]
        viewModel.updateQuantity(cartItem, 5)

        viewModel.cartItems.test {
            val cart = awaitItem()
            assertEquals(5, cart[0].quantity)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `updateQuantity to zero removes item`() = runTest {
        val item = sampleItems[0]
        viewModel.addToCart(item)
        val cartItem = viewModel.cartItems.value[0]
        viewModel.updateQuantity(cartItem, 0)

        viewModel.cartItems.test {
            val cart = awaitItem()
            assertTrue(cart.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `clearCart empties the cart`() = runTest {
        viewModel.addToCart(sampleItems[0])
        viewModel.addToCart(sampleItems[1])
        viewModel.clearCart()

        viewModel.cartItems.test {
            val cart = awaitItem()
            assertTrue(cart.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }
}
