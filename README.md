# Android Kiosk Menu Application

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2028+-3DDC84?logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Realtime%20DB-FFCA28?logo=firebase&logoColor=black)
![Hilt](https://img.shields.io/badge/Hilt-2.59.1-2196F3)

A production-oriented Android kiosk app for restaurant ordering, built with Jetpack Compose, Hilt, Room, and Firebase Realtime Database.

The app runs as a full-screen kiosk with admin unlock controls, dynamic theming from Firebase settings, and order logging for completed payments.

## Current Features

- **Single-screen kiosk flow** — Menu browsing, item details, cart, checkout, and payment overlays are handled in one Compose flow (`MenuScreen`)
- **Three menu layouts** — Select between `CURRENT`, `NEW_HORIZONTAL`, and `PORTRAIT` modes at runtime
- **Category + best seller browsing** — Category pages, side panel navigation, and featured items
- **Order pipeline** — Add to cart, quantity updates, checkout confirmation, payment method selection (QR/Counter)
- **Firebase-backed data** — Menu categories, app settings (theme/background), and order logs
- **Offline-first reads** — Room cache with Firebase sync
- **Kiosk hardening** — Lock task mode, persistent home behavior, boot auto-launch, blocked system escape routes (when Device Owner is provisioned)
- **Admin unlock controls** — PIN dialog trigger via long-press volume up or secret corner taps; encrypted PIN storage with lockout policy
- **Observability** — Timber logging + Firebase Crashlytics/Analytics

## Screenshots

![Current App Screenshot](screenshot.png)

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 2.3.0 |
| UI Framework | Jetpack Compose (Material3) | BOM 2026.01.00 |
| Dependency Injection | Hilt (Dagger) | 2.59.1 |
| Annotation Processing | KSP | 2.3.5 |
| Local Database | Room | 2.7.1 |
| Remote Database | Firebase Realtime Database | BOM 34.8.0 |
| Authentication | Firebase Auth (Anonymous) | BOM 34.8.0 |
| Crash Reporting | Firebase Crashlytics | BOM 34.8.0 |
| Analytics | Firebase Analytics | BOM 34.8.0 |
| Image Loading | Coil 3 | 3.3.0 |
| Media | Media3 ExoPlayer | 1.9.1 |
| Networking (legacy/internal) | Retrofit 2 + OkHttp | 2.11.0 / 4.12.0 |
| Security | EncryptedSharedPreferences | 1.1.0-alpha06 |
| Logging | Timber | 5.0.1 |
| Testing | JUnit4, MockK, Turbine, Coroutines Test | - |
| Build System | Gradle Kotlin DSL + Version Catalog | AGP 9.0.0 |
| Min SDK | Android 9.0 | API 28 |
| Target / Compile SDK | Android 16 | API 36 |

## Architecture Overview

The project follows a clean layered structure with MVVM:

- **UI layer (`ui/`)**: Compose screens/components, overlays, theming, animation tokens
- **Domain layer (`domain/repository/`)**: Repository interfaces
- **Data layer (`data/`)**: Firebase + Room implementations and mapping
- **Admin layer (`admin/`)**: Kiosk mode management, Device Admin, PIN/auth and unlock logging
- **DI layer (`di/`)**: Hilt modules and app startup wiring

### Runtime flow

1. `MenuApplication` initializes Firebase persistence and anonymous sign-in
2. `MainActivity` enforces kiosk behavior and hosts Compose content
3. `MenuViewModel` streams categories, best sellers, and app settings
4. User completes checkout and selects payment
5. Completed order is logged to Firebase

## Firebase Data Paths

Current app reads/writes these Realtime Database paths:

- `branch2/categories` — Menu categories and items
- `branch2/appSettings` — Background image/theme settings
- `branch2/logs/{orderNumber}` — Completed order logs

Anonymous auth is required before listeners/writes attach. This project expects Firebase rules compatible with authenticated reads (for example, `auth != null`).

## Kiosk / Admin Notes

- Main activity is registered as launcher + home category
- Device Admin receiver is declared with `device_admin_policies`
- Boot receiver relaunches the app after reboot (`BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`)
- Device Owner mode enables full lock task enforcement via `KioskManager`

### Device Owner provisioning (development/provisioning step)

```bash
adb shell dpm set-device-owner com.example.androidkiosk/.admin.KioskDeviceAdminReceiver
```

If Device Owner is not set, the app still runs in a degraded development mode.

## Project Structure

```text
app/src/main/java/com/example/androidkiosk/
├── admin/                  # Kiosk/device-admin/auth/PIN utilities
├── data/                   # Local/remote data sources + repository impl
├── di/                     # Hilt modules + MenuApplication
├── domain/repository/      # Repository contracts
├── model/                  # App and domain models
├── ui/
│   ├── main/               # MainActivity
│   ├── menu/               # MenuScreen + MenuViewModel + components
│   ├── animation/          # Motion/shimmer/stagger utilities
│   └── theme/              # Dynamic theme/background primitives
└── util/                   # App utility helpers
```

## Prerequisites

- Android Studio (recent stable)
- JDK 17+
- Android SDK configured in `local.properties`
- Firebase project (Realtime Database + Auth + Crashlytics/Analytics)
- `app/google-services.json` present

## Getting Started

1. **Clone**
   ```bash
   git clone https://github.com/Liliwqt/E-Menu.git
   cd E-Menu
   ```

2. **Configure Firebase**
   - Add your `google-services.json` to `app/`
   - Ensure Realtime Database and Anonymous Auth are enabled

3. **Configure local SDK path**
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```

4. **Run debug build**
   ```bash
   ./gradlew installDebug
   ```

## Build, Test, Release

### Unit tests
```bash
./gradlew test
```

### Instrumented tests
```bash
./gradlew connectedAndroidTest
```

### Release build
```bash
./gradlew assembleRelease
```

For release signing, copy `keystore.properties.example` to `keystore.properties` and fill your values.

## Notes

- Weather feature logic has been removed from active app behavior.
- Legacy networking dependencies remain in Gradle for internal/compatibility needs.
