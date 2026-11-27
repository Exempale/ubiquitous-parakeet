# Limbus FPS Unlocker — diagnostic companion prototype

This is a small Android Studio project for a no-root, no-extra-permission companion app.

## What it does
- Reads the phone's current display refresh rate.
- Lists display modes reported by Android.
- Checks whether the Limbus Company package can be launched.
- Opens Android Display settings or launches the game.

## What it does NOT do
- It does not unlock the game's internal 60 FPS cap.
- It does not measure Limbus Company FPS.
- It does not inject code, modify the game APK, draw an overlay, or run in the background.

Android's `Surface.setFrameRate()` is scoped to the calling app's own display surface. It cannot set the frame rate of another app's surface. A regular third-party app also cannot inspect another app's rendered frame count without privileged access or instrumentation.

## Build
1. Open this folder in Android Studio (recent version).
2. Allow Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install the debug APK on the phone.

The project uses compileSdk 35 and Android Gradle Plugin 8.7.3. If Android Studio proposes a compatible Gradle/JDK update, accept its recommendation.

## Package ID check
The prototype checks `com.ProjectMoon.LimbusCompany`. If the Play Store build on your device uses a different package ID, update `GAME_PACKAGE` in `MainActivity.kt` and the matching `<queries>` entry in `AndroidManifest.xml`.


## Build APK with GitHub Actions
1. Create a GitHub repository and upload the contents of this folder.
2. Open the repository's Actions tab.
3. Run **Build Android APK** (or push to `main`).
4. Open the completed workflow run and download the `LimbusFPSUnlocker-debug` artifact.
