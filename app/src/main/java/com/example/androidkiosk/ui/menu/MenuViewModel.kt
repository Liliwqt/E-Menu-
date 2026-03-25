package com.example.androidkiosk.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidkiosk.domain.repository.AppSettingsRepository
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.domain.repository.OrderRepository
import com.example.androidkiosk.model.AppSettings
import com.example.androidkiosk.model.CartItem
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.model.Order
import com.example.androidkiosk.model.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val orderRepository: OrderRepository,
    appSettingsRepository: AppSettingsRepository  // plain param — only used in property initializer
) : ViewModel() {

    
    val categories: StateFlow<List<CategoryWithItems>> = menuRepository
        .observeCategories()
        .catch { e ->
            Timber.e(e, "Error observing categories")
            _errorMessage.value = "Unable to load menu. Please try again."
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    
    val bestSellers: StateFlow<List<MenuItem>> = menuRepository
        .observeBestSellers()
        .catch { e -> Timber.e(e, "Error observing best sellers") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings> = appSettingsRepository
        .observeAppSettings()
        .catch { e -> Timber.e(e, "Error observing app settings") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)

    companion object {
        private const val MAX_ITEM_QUANTITY = 99
    }

    init {
        observeMenuData()
    }

    private fun observeMenuData() {
        viewModelScope.launch {
            menuRepository.observeCategories()
                .catch { _ ->
                    _errorMessage.value = "Unable to load menu. Please try again."
                    _isLoading.value = false
                }
                .collect { cats ->
                    if (cats.isNotEmpty()) {
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
                }
        }
    }

    fun retryLoading() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            menuRepository.refresh()
        }
    }

    fun addToCart(item: MenuItem) {
        _cartItems.update { currentCart ->
            val existingIndex = currentCart.indexOfFirst { it.menuItem.id == item.id }
            if (existingIndex >= 0) {
                val existing = currentCart[existingIndex]
                if (existing.quantity >= MAX_ITEM_QUANTITY) return@update currentCart
                currentCart.toMutableList().apply {
                    this[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                }
            } else {
                currentCart + CartItem(
                    menuItem = item,
                    name = item.name,
                    price = item.price,
                    quantity = 1
                )
            }
        }
    }

    fun addToCartWithQuantity(item: MenuItem, quantity: Int) {
        if (quantity <= 0) return
        _cartItems.update { currentCart ->
            val existingIndex = currentCart.indexOfFirst { it.menuItem.id == item.id }
            if (existingIndex >= 0) {
                val existing = currentCart[existingIndex]
                val newQty = (existing.quantity + quantity).coerceAtMost(MAX_ITEM_QUANTITY)
                currentCart.toMutableList().apply {
                    this[existingIndex] = existing.copy(quantity = newQty)
                }
            } else {
                currentCart + CartItem(
                    menuItem = item,
                    name = item.name,
                    price = item.price,
                    quantity = quantity.coerceAtMost(MAX_ITEM_QUANTITY)
                )
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.update { currentCart ->
            currentCart.filter { it.menuItem.id != cartItem.menuItem.id }
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
            return
        }
        val clampedQuantity = newQuantity.coerceAtMost(MAX_ITEM_QUANTITY)
        _cartItems.update { currentCart ->
            val index = currentCart.indexOfFirst { it.menuItem.id == cartItem.menuItem.id }
            if (index >= 0) {
                currentCart.toMutableList().apply {
                    this[index] = this[index].copy(quantity = clampedQuantity)
                }
            } else {
                currentCart
            }
        }
    }

    fun clearCart() {
        _cartItems.update { emptyList() }
    }

    fun confirmOrder(customerName: String): Order {
        val items = _cartItems.value
        val total = items.sumOf { it.menuItem.price * it.quantity }
        val orderNumber = UUID.randomUUID().toString().take(8).uppercase()
        val order = Order(
            orderNumber = orderNumber,
            customerName = customerName.trim(),
            items = items,
            total = total
        )
        _currentOrder.value = order
        clearCart()
        return order
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _currentOrder.value = _currentOrder.value?.copy(paymentMethod = method)
        // Log the completed order to Firebase at branch2/logs/{orderNumber}
        _currentOrder.value?.let { order ->
            viewModelScope.launch {
                orderRepository.logOrder(order).onFailure { e ->
                    Timber.e(e, "Failed to log order %s", order.orderNumber)
                }
            }
        }
    }

    fun resetOrderFlow() {
        _currentOrder.value = null
    }
}
