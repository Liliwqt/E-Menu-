# Android Kiosk Menu Application

An Android self-service restaurant kiosk built with Jetpack Compose, Hilt, Room, and Firebase Realtime Database. It provides three responsive menu layouts, size-aware inventory, a local offline menu cache, static GCash QR checkout, pay-at-counter ordering, and device-owner kiosk controls.

> [!IMPORTANT]
> The bundled QR flow cannot confirm bank settlement. Tapping **I've Paid** records a customer claim as `CUSTOMER_REPORTED_PAID`; staff must verify the payment independently.

## Features

- Three selectable menu layouts: current, horizontal, and portrait
- Firebase-backed categories, sizes, inventory, appearance settings, and order logs
- Numeric and `{ "priceModifier": number }` size-format compatibility
- Stock indicators, per-size cart lines, quantity caps, and size-adjusted totals
- Room-backed menu caching for temporary network interruptions
- Static merchant InstaPay/GCash QR and pay-at-counter checkout
- UUID-backed, retry-safe order submission with atomic tracked-stock updates
- Device-owner lock-task mode, boot launch, secure-screen protection, and PIN-gated admin access
- Persistent failed-PIN throttling with a 60-second lockout
- Manual anonymous-UID authorization with a registration/status screen
- Exact-bucket Firebase Storage image validation and local image fallbacks

## Requirements

- Android Studio with Android SDK 36
- JDK 21 for Gradle and Firebase Emulator tests; app bytecode targets Java 11
- Node.js 20 or 22 for Firebase rules tests
- Android 9 (API 28) or newer
- A Firebase project with Realtime Database and Anonymous Authentication enabled
- A physical device or emulator for instrumentation tests
- A freshly provisioned device if testing device-owner kiosk mode

## Project Structure

```text
app/src/main/java/com/example/androidkiosk/
├── admin/       Device-owner, PIN, boot, and authentication controls
├── data/        Firebase and Room implementations
├── di/          Hilt modules and application setup
├── domain/      Repository interfaces
├── model/       Menu, cart, order, payment, and settings models
├── ui/          Compose screens, components, animation, and themes
└── util/        Input and image URL validation
```

Room schemas are versioned in `app/schemas/`. Deployment guidance and example Firebase rules are in [`SECURITY.md`](SECURITY.md).

## Setup

1. Clone the repository:

```bash
git clone https://github.com/Liliwqt/E-Menu-.git
cd E-Menu-
```

2. Download `google-services.json` from the Firebase console and place it at `app/google-services.json`.

This file is intentionally ignored by Git and must not be committed.

3. If Android Studio has not created it, add the local SDK path to `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

4. Confirm that `app/src/main/res/drawable-nodpi/merchant_qr.png` belongs to the intended merchant before building a deployable kiosk.

5. Build and install the debug application:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

6. On first launch, copy the anonymous UID displayed by the registration screen. Firebase Authentication creates this anonymous user automatically. Add the UID to each allowlist expression in your deployed `branch2` rules, then tap **Check registration** in the app.

The tracked `database.rules.json` contains nonfunctional `REPLACE_WITH_KIOSK_UID_*` values so real device identifiers are not published. Replace or extend those values locally before testing and deployment, and do not commit the populated version.

## Firebase Data Contract

The application uses the `branch2` root:

```text
branch2/
├── categories/{category}/{item}
│   └── sizes/{size} = number | { priceModifier: number }
├── inventory/{category}/{item}/sizes/{size}/stock
├── appSettings
│   ├── backgroundImage
│   └── backgroundTheme
└── logs/{orderId}
```

Example menu and inventory data:

```json
{
  "branch2": {
    "categories": {
      "Drinks": {
        "coffee": {
          "id": "coffee",
          "name": "Coffee",
          "price": 100,
          "available": true,
          "sizes": {
            "Medium": 0,
            "Large": { "priceModifier": 25 }
          }
        }
      }
    },
    "inventory": {
      "Drinks": {
        "coffee": {
          "sizes": {
            "Medium": { "stock": 10 },
            "Large": { "stock": 4 }
          }
        }
      }
    }
  }
}
```

Missing item inventory records are treated as legacy/untracked. Once an item has an inventory record, a missing selected-size record is treated as zero stock.

Orders are written to `branch2/logs/{orderId}` with a full UUID, eight-character display number, customer name, size-adjusted line items, total, payment method/status, authenticated kiosk UID, and Firebase server timestamp.

## Payment Behavior

- **GCash / InstaPay QR:** displays the bundled merchant QR. **I've Paid** submits the order as `CUSTOMER_REPORTED_PAID`.
- **Pay at counter:** submits the order as `PAY_AT_COUNTER` before directing the customer to staff.
- Order submission and tracked inventory decrements use one Firebase multi-location update.
- A retry reuses the same UUID and checks the existing log before applying inventory changes again.

There is no PayMongo or other payment-provider secret in the mobile client. Any previously used PayMongo key must still be revoked because removing it from source does not invalidate old copies.

## Kiosk Provisioning

Device-owner provisioning normally requires a factory-reset or otherwise unprovisioned test device. With the application installed, run:

```bash
adb shell dpm set-device-owner com.example.androidkiosk/.admin.KioskDeviceAdminReceiver
```

Debug builds remain usable with a visible development state when the app is not device owner. Release builds block ordering unless device-owner lock task is active.

The configured admin PIN is intentionally `1234`. This is a documented residual risk even though failed attempts and lockout expiry persist across dialog dismissal and process restarts.

## Build and Test

```bash
# Local JVM tests
./gradlew testDebugUnitTest

# Android lint
./gradlew lintDebug

# Compile instrumentation and Compose UI tests
./gradlew compileDebugAndroidTestKotlin

# Run instrumentation/UI tests with a connected device or emulator
./gradlew connectedDebugAndroidTest

# Debug APK
./gradlew assembleDebug

# Minified, resource-shrunk release APK
./gradlew assembleRelease

# Firebase Realtime Database rules (requires JDK 21 and Node.js 20/22)
cd firebase-tests
npm ci --ignore-scripts
npm run test:emulator
```

Generated APKs are placed under `app/build/outputs/apk/`.

## Release Signing

Copy the tracked template and keep the populated file private:

```bash
cp keystore.properties.example keystore.properties
```

Set `storeFile`, `storePassword`, `keyAlias`, and `keyPassword` in `keystore.properties`. Signing keystores, `keystore.properties`, `local.properties`, and Firebase configuration are ignored by Git.

## Security Before Deployment

Complete the checklist in [`SECURITY.md`](SECURITY.md), including:

- Deploy and emulator-test least-privilege Realtime Database rules.
- Add every kiosk UID manually and remove it when the device is retired or reinstalled.
- Do not grant `.write` at `branch2`; a parent grant bypasses the narrower log and stock rules.
- Verify nonnegative-stock and immutable-order rules.
- Rotate any PayMongo key that was previously packaged in an APK.
- Manually verify the bundled merchant QR.
- Define a retention/deletion period for customer names and order logs.

The client-side safeguards do not replace a trusted backend when authoritative payment verification or fraud-resistant inventory enforcement is required.

App Check is intentionally not included in this pre-production release. Anonymous UID allowlisting reduces access but does not attest that requests came from an untampered application.

## Publishing to GitHub

Before publishing, confirm that `git status` does not include `google-services.json`, `local.properties`, a signing keystore, `keystore.properties`, IDE metadata, or anything under a `build/` directory.

```bash
git status
git diff --check
bash scripts/security_scan.sh
git add -A
git status
git commit -m "Harden kiosk ordering and clean project"
git push -u origin main
```

Do not add ignored credential files with `git add -f`. If an old PayMongo secret was ever committed or distributed in an APK, removing it from the current tree is insufficient—revoke and rotate it before publishing.

The GitHub workflow uses `app/google-services.example.json`, never the real Firebase configuration. It runs Android tests, lint, debug/release assembly, dependency audit, Firebase rules tests, and credential scans without repository secrets.
