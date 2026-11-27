# Limbus FPS Unlocker

A no-root, no-extra-permission companion app for Nothing Phone (2a) Plus and Limbus Company.

## Current app features
- Shows the display's current refresh rate in Hz.
- Lists refresh-rate modes exposed by Android.
- Checks whether Limbus Company is installed and can be launched.
- Shortcuts to Android Display settings and Developer options.
- Custom neon FPS launcher icon.

## Important limitation
This app does **not** unlock Limbus Company's internal FPS cap and does **not** read the game's true rendered FPS. Android isolates apps from one another: a normal third-party APK cannot change another app's Unity runtime, modify its rendering surface, or issue privileged GameManager shell commands.

A real game-side unlock would require either a change by Project Moon, or an explicitly authorized system-level mechanism (for example, an ADB/Shizuku shell action or a rooted runtime module). Those are outside this project's current no-extra-permission requirement. The app does not pretend that the display's Hz reading is the game's FPS.

## Build locally
1. Open this folder in Android Studio.
2. Allow Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install `app/build/outputs/apk/debug/app-debug.apk`.

## Automatic GitHub builds and Releases
Every push to `main` builds an installable debug APK and publishes a GitHub Release named `Limbus FPS Unlocker Build #N`, with `LimbusFPSUnlocker.apk` attached.

You can also push a version tag matching `v*` (for example `v0.1.0`) to publish a named version release.

The workflow requires repository Actions to be enabled and `contents: write` permission. The APK is debug-signed by the ephemeral GitHub runner, so a later build may not install as an in-place update over an earlier build; uninstall the old debug build first if Android reports a signature conflict.

## Technical details
- Application ID: `dev.limbus.fpsunlocker`
- Limbus package checked: `com.ProjectMoon.LimbusCompany`
- Compile SDK: 35
- Java/Kotlin bytecode target: 17
- Android Gradle Plugin: 8.7.3
