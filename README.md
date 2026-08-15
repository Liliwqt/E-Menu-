# E-Menu Android Kiosk

E-Menu is a self-service restaurant kiosk for Android. It supports multiple menu layouts, size-based pricing and inventory, offline menu caching, QR checkout, pay-at-counter orders, and device-owner kiosk controls.

> The QR payment screen displays a static merchant QR. Selecting **I've Paid** records the customer's confirmation; it does not verify settlement with a bank or payment provider.

## Features

- Three menu layouts for different screen orientations
- Firebase Realtime Database menu, inventory, settings, and order data
- Room cache for temporary network interruptions
- Size selection with price modifiers and per-size stock limits
- Static GCash/InstaPay QR and pay-at-counter checkout
- Retry-safe order submission using UUID order IDs
- Anonymous Firebase authentication with a manual UID allowlist
- Device-owner lock task, boot launch, secure-screen protection, and PIN-gated maintenance access
- Firebase Storage image validation with bundled fallbacks

## Requirements

- Android Studio and Android SDK 36
- JDK 21
- Android 9 (API 28) or newer
- Node.js 20 or 22 for Firebase Rules tests
- Firebase Realtime Database with Anonymous Authentication enabled

## Setup

1. Clone the repository.

   ```bash
   git clone https://github.com/Liliwqt/E-Menu-.git
   cd E-Menu-
   ```

2. Download `google-services.json` from Firebase and place it in `app/`.

3. Add your Android SDK path to `local.properties` if Android Studio has not created it.

   ```properties
   sdk.dir=/absolute/path/to/Android/Sdk
   ```

4. Verify that `app/src/main/res/drawable-nodpi/merchant_qr.png` contains the correct merchant QR.

5. Build the debug APK.

   ```bash
   ./gradlew assembleDebug
   ```

`google-services.json`, `local.properties`, signing properties, and keystores are intentionally excluded from Git.

## Firebase access

The application signs in anonymously on first launch and displays its Firebase UID. Add that UID to the allowlist in your deployed Realtime Database Rules, then select **Check registration** on the kiosk.

The committed [`database.rules.json`](database.rules.json) uses placeholder UIDs. Replace them only in the copy you deploy; do not commit real device UIDs.

Application data is stored below `branch2`:

```text
branch2/
├── categories/{category}/{item}
│   └── sizes/{size} = number | { priceModifier: number }
├── inventory/{category}/{item}/sizes/{size}/stock
├── appSettings
└── logs/{orderId}
```

An item without an inventory record is treated as untracked. If an inventory record exists, any missing size is treated as out of stock.

## Kiosk provisioning

Device-owner mode normally requires a reset or unprovisioned Android device. Install the app, then run:

```bash
adb shell dpm set-device-owner com.example.androidkiosk/.admin.KioskDeviceAdminReceiver
```

Release builds require device-owner lock task before ordering is enabled. Debug builds show a development warning when device-owner mode is unavailable.

The default maintenance PIN is `1234`. Change it before deploying the kiosk in a public location.

## Build and test

```bash
# Unit tests
./gradlew testDebugUnitTest

# Lint and instrumentation test compilation
./gradlew lintDebug compileDebugAndroidTestKotlin

# Debug and release APKs
./gradlew assembleDebug assembleRelease

# Firebase Realtime Database Rules tests
cd firebase-tests
npm ci --ignore-scripts
npm run test:emulator
```

APK output is written to `app/build/outputs/apk/`.

For a signed release, copy `keystore.properties.example` to `keystore.properties` and fill in the local signing values. Never commit the populated file or the keystore.

## Security

Read [`SECURITY.md`](SECURITY.md) before deploying or publishing a release. At minimum:

- Deploy restrictive Realtime Database Rules.
- Allow only approved kiosk UIDs.
- Use a unique maintenance PIN.
- Verify the bundled merchant QR.
- Keep API keys and signing credentials outside the application and repository.
- Define how long customer names and order records are retained.

Run the repository credential check before publishing:

```bash
bash scripts/security_scan.sh
```

This client records customer-reported QR payments and updates inventory directly. Use a trusted backend if payment verification or stronger inventory enforcement is required.
