package com.example.androidkiosk.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.domain.repository.WeatherRepository
import com.example.androidkiosk.model.CartItem
import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import com.example.androidkiosk.model.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ====================================================
 * MenuViewModel — Professional Architecture
 * ====================================================
 *
 * Responsibilities:
 * - Exposes UI state via StateFlow
 * - Delegates data fetching to repositories (injected by Hilt)
 * - Manages shopping cart state
 *
 * This ViewModel does NOT:
 * - Know about Firebase, Retrofit, or Room (that's the repository's job)
 * - Create its own dependencies (Hilt handles that)
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    // ==========================================
    // MENU STATE (from Room via Repository)
    // ==========================================

    /** All categories with items, streamed from the local Room cache. */
    val categories: StateFlow<List<CategoryWithItems>> = menuRepository
        .observeCategories()
        .catch { e ->
            Timber.e(e, "Error observing categories")
            _errorMessage.value = "Error loading menu: ${e.message}"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Best-seller items, streamed from the local Room cache. */
    val bestSellers: StateFlow<List<MenuItem>> = menuRepository
        .observeBestSellers()
        .catch { e -> Timber.e(e, "Error observing best sellers") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // UI STATE
    // ==========================================

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ==========================================
    // WEATHER STATE
    // ==========================================

    private val _weather = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weather: StateFlow<WeatherUiState> = _weather.asStateFlow()

    // ==========================================
    // CART STATE
    // ==========================================

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        observeMenuData()
        fetchWeather()
        startWeatherPolling()
    }

    // ==========================================
    // DATA LOADING
    // ==========================================

    private fun observeMenuData() {
        viewModelScope.launch {
            // The categories flow from the repository will emit once Room has data.
            // We start as loading and flip to false once we get the first emission.
            menuRepository.observeCategories()
                .catch { e ->
                    _errorMessage.value = "Error loading menu: ${e.message}"
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

    private fun fetchWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            _weather.value = weatherRepository.getCurrentWeather()
        }
    }

    private fun startWeatherPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(10 * 60 * 1000L) // 10 minutes
                _weather.value = weatherRepository.getCurrentWeather()
            }
        }
    }

    fun retryLoading() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            menuRepository.refresh()
        }
        fetchWeather()
    }

    // ==========================================
    // CART MANAGEMENT
    // ==========================================

    fun addToCart(item: MenuItem) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.menuItem.id == item.id }

        if (existingItem != null) {
            val index = currentCart.indexOf(existingItem)
            currentCart[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentCart.add(
                CartItem(
                    menuItem = item,
                    name = item.name,
                    price = item.price,
                    quantity = 1
                )
            )
        }
        _cartItems.value = currentCart
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.value = _cartItems.value.filter { it.menuItem.id != cartItem.menuItem.id }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
            return
        }

        val currentCart = _cartItems.value.toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == cartItem.menuItem.id }

        if (index >= 0) {
            currentCart[index] = currentCart[index].copy(quantity = newQuantity)
            _cartItems.value = currentCart
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

