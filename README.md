# Android Kiosk Menu Application

A production-oriented Android kiosk app for restaurant ordering built with Jetpack Compose, Hilt, Room, and Firebase Realtime Database. This repository contains the app source under `app/` and configuration for CI and releases.

## Summary

- Full-screen kiosk UI with admin unlock controls and device-owner provisioning
- Firebase-backed categories, settings, and order logging with local Room caching
- Compose-based UI components and theming for flexible presentation modes

## Quick Start

1. Clone the repo:

```bash
git clone <your-repo-url>
cd <repo-folder>
```

2. Add Firebase config:

- Place `google-services.json` into `app/`.
- Enable Realtime Database and Anonymous Auth in your Firebase project.

3. Configure SDK path (if needed):

```properties
sdk.dir=/path/to/Android/Sdk
```

4. Build and install debug:

```bash
./gradlew installDebug
```

## Important Files

- `app/` — Android application module
- `app/src/main/java/com/example/androidkiosk/` — source packages (admin, data, di, domain, ui)
- `keystore.properties.example` — template for release signing

## Development Notes

- Device owner provisioning (development only):

```bash
adb shell dpm set-device-owner com.example.androidkiosk/.admin.KioskDeviceAdminReceiver
```

- The app expects authenticated Firebase access (anonymous auth used by default).

## Tests and Build

- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest`
- Release build: `./gradlew assembleRelease`

## Pushing this repository to GitHub (new repo)

If you want to create and push a new repository on GitHub, use the commands below. Alternatives:

- Create a repo on GitHub manually and run:

```bash
git remote remove origin || true
git remote add origin https://github.com/<your-username>/<new-repo>.git
git branch -M main
git add -A
git commit -m "Clean comments and update README"
git push -u origin main
```

- Or, if `gh` is installed and authenticated, run:

```bash
gh repo create <your-username>/<new-repo> --public --source=. --remote=origin --push
```

Tell me if you want to create the GitHub repo now; the remote can be set and pushed once the repository URL is ready.

---

Next steps:

1. Open a quick summary of files with multi-line comments.
2. Apply the comment-cleaning script.
3. Create and push a new GitHub repo for this project.
