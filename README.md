# Android Kiosk Menu Application

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2028+-3DDC84?logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Realtime%20DB-FFCA28?logo=firebase&logoColor=black)
![Hilt](https://img.shields.io/badge/Hilt-2.59.1-2196F3)
![License](https://img.shields.io/badge/License-MIT-green)

A modern restaurant/food kiosk Android application built with Jetpack Compose and Clean Architecture. Designed for Cebu City-area restaurants, the app provides an immersive, full-screen kiosk experience for browsing menus, viewing best sellers, checking live weather, and placing orders.

## Features

- **Menu Browsing** — Browse food items organized by category with horizontal scrollable rows
- **Best Sellers Carousel** — Infinite-scrolling hero carousel showcasing popular items (Material3 `HorizontalCenteredHeroCarousel`)
- **Live Weather Widget** — Real-time weather data for Cebu City via Open-Meteo API, with looping MP4 video background powered by ExoPlayer
- **Item Detail Overlay** — Fullscreen animated overlay with item details and "Add to Cart" action
- **Shopping Cart** — Add, remove, and update item quantities with animated cart overlay
- **Checkout Flow** — Order summary with confirmation
- **Offline-First** — Local Room database cache with Firebase Realtime Database sync in the background
- **Immersive Kiosk Mode** — Hidden system bars, edge-to-edge display, screen always on, portrait-locked
- **Crash Reporting & Analytics** — Firebase Crashlytics and Firebase Analytics integration

## Screenshots

<!-- Add your screenshots here -->
<!-- ![Menu Screen](screenshots/menu.png) -->
<!-- ![Cart Screen](screenshots/cart.png) -->
<!-- ![Checkout Screen](screenshots/checkout.png) -->

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 2.3.0 |
| UI Framework | Jetpack Compose (Material3) | BOM 2026.01.00 |
| Dependency Injection | Hilt (Dagger) | 2.59.1 |
| Annotation Processing | KSP | 2.3.5 |
| Navigation | Navigation Compose (type-safe routes) | 2.9.6 |
| Networking | Retrofit 2 + OkHttp | 2.11.0 / 4.12.0 |
| Serialization | Kotlinx Serialization | 1.8.1 |
| Local Database | Room | 2.7.1 |
| Remote Database | Firebase Realtime Database | 22.0.1 |
| Crash Reporting | Firebase Crashlytics | 19.4.2 |
| Analytics | Firebase Analytics | 22.5.0 |
| Image Loading | Coil 3 | 3.3.0 |
| Video Player | Media3 ExoPlayer | 1.9.1 |
| Logging | Timber | 5.0.1 |
| Testing | JUnit 4, MockK 1.13.16, Turbine 1.2.0, Coroutines Test | - |
| Build System | Gradle (Kotlin DSL) + Version Catalog | AGP 9.0.0 |
| Min SDK | 28 (Android 9.0) | |
| Target / Compile SDK | 36 | |

## Architecture

The project follows **Clean Architecture** with the **MVVM** pattern, organized into three layers:

```
┌─────────────────────────────────────────────────┐
│                 Presentation (ui/)              │
│  MenuScreen, CartScreen, CheckoutScreen         │
│  MenuViewModel, Components, Theme, Navigation   │
├─────────────────────────────────────────────────┤
│                  Domain (domain/)               │
│  MenuRepository (interface)                     │
│  WeatherRepository (interface)                  │
├─────────────────────────────────────────────────┤
│                   Data (data/)                  │
│  MenuRepositoryImpl, WeatherRepositoryImpl      │
│  Room (local), Firebase + Retrofit (remote)     │
└─────────────────────────────────────────────────┘
```

**Data Flow:**
```
Firebase Realtime DB ──► RepositoryImpl ──► Room (cache) ──► Flow ──► ViewModel ──► Compose UI
Open-Meteo API ────────► WeatherRepo ──────────────────────► Flow ──► ViewModel ──► Compose UI
```

## Project Structure

```
app/src/main/java/com/example/androidkiosk/
├── di/                          # Dependency Injection
│   ├── MenuApplication.kt      # Hilt Application class
│   ├── DatabaseModule.kt       # Room database provider
│   ├── NetworkModule.kt        # Retrofit & OkHttp provider
│   ├── FirebaseModule.kt       # Firebase instance provider
│   └── RepositoryModule.kt     # Repository bindings
├── model/                       # Domain models
│   ├── MenuItem.kt              # Menu item data class
│   └── Weather.kt               # Weather data classes
├── domain/                      # Domain layer
│   └── repository/
│       ├── MenuRepository.kt    # Menu repository interface
│       └── WeatherRepository.kt # Weather repository interface
├── data/                        # Data layer
│   ├── local/
│   │   ├── MenuDatabase.kt     # Room database
│   │   ├── dao/
│   │   │   └── MenuItemDao.kt  # Data access object
│   │   └── entity/
│   │       └── MenuItemEntity.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── WeatherApiService.kt  # Retrofit API
│   │   └── dto/
│   │       └── WeatherDto.kt         # API response DTOs
│   └── repository/
│       ├── MenuRepositoryImpl.kt     # Menu repo implementation
│       └── WeatherRepositoryImpl.kt  # Weather repo implementation
└── ui/                          # Presentation layer
    ├── main/
    │   └── MainActivity.kt     # Single Activity (Compose)
    ├── menu/
    │   ├── MenuScreen.kt       # Main menu screen
    │   ├── MenuViewModel.kt    # Menu state management
    │   └── components/
    │       ├── BestSellersSection.kt
    │       ├── CategorySection.kt
    │       ├── WeatherSection.kt
    │       ├── ItemDetailOverlay.kt
    │       └── LoadingErrorScreens.kt
    ├── cart/
    │   └── CartScreen.kt       # Shopping cart
    ├── checkout/
    │   └── CheckoutScreen.kt   # Checkout flow
    ├── navigation/
    │   ├── Route.kt            # Type-safe route definitions
    │   └── AppNavHost.kt       # Navigation graph
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Prerequisites

- **Android Studio** Meerkat (2024.3.1) or later
- **JDK 17** or higher
- A **Firebase project** with Realtime Database enabled
- `google-services.json` placed in `app/`

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/android-kiosk-menu.git
   cd android-kiosk-menu
   ```

2. **Set up Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable **Realtime Database** and **Crashlytics**
   - Download `google-services.json` and place it in the `app/` directory

3. **Configure local properties**
   - Ensure `local.properties` has your Android SDK path:
     ```properties
     sdk.dir=/path/to/your/Android/Sdk
     ```

4. **Open in Android Studio**
   - Open the project root folder
   - Wait for Gradle sync to complete

5. **Run the app**
   ```bash
   ./gradlew installDebug
   ```
   Or use the Run button in Android Studio.

## Configuration

### Release Signing

For release builds, create a `keystore.properties` file in the project root (see `keystore.properties.example`):

```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=/path/to/your/keystore.jks
```

### Firebase Database Structure

The app expects the following structure in Firebase Realtime Database:

```json
{
  "menuItems": {
    "item_id": {
      "name": "Item Name",
      "description": "Item description",
      "price": 99.0,
      "imageUrl": "https://...",
      "categoryName": "Category",
      "isBestSeller": true
    }
  }
}
```

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

The test suite uses **MockK** for mocking, **Turbine** for Flow testing, and **Coroutines Test** for structured concurrency testing. Tests cover ViewModel logic including menu loading, cart operations (add/remove/update quantity), and error handling.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
