# Limbus FPS Unlocker

A Shizuku-powered GameManager configuration helper for Android.

## Current app features
- Shows the phone's current display refresh rate and supported modes.
- Checks that Limbus Company is installed and can launch it.
- Checks Shizuku availability and runtime permission.
- Runs short Android shell commands through Shizuku's `newProcess` API (no custom UserService Binder).
- Reads the current `game_overlay` value, saves a backup, applies a Performance-mode `fps=120` value, and verifies the write.
- Restores the previous `game_overlay` value.
- Copies diagnostic logs to the clipboard.
- Custom neon FPS launcher icon.

## Important FPS limitation
Android's GameManager `fps` intervention is **FPS throttling**: it can cap a game to a supported value, but it cannot turn a game's own 60 FPS limit into 120 FPS. The setting does not modify Limbus Company's Unity runtime. A successful command only confirms that the system configuration was written; actual in-game FPS must be measured separately.

The supported intervention values depend on the device's display mode. For a 120 Hz display, Android documents 120/60/40/30 as supported throttle values. See the Android developer documentation linked below.

## Shizuku
1. Install and start Shizuku using Wireless debugging.
2. Open Limbus FPS Unlocker.
3. Grant the app access in Shizuku when prompted.
4. Use **Apply Game Mode 120 FPS** to set and verify the GameManager configuration.
5. Use **Restore settings** to restore the saved previous value.

The app uses Shizuku's one-shot process API for three short commands. It intentionally no longer binds a custom Shizuku UserService, because that binding did not deliver a Binder callback on the tested Nothing Phone (2a) Plus / Android 16 configuration.

## Build locally
1. Open this folder in Android Studio.
2. Allow Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install `app/build/outputs/apk/debug/app-debug.apk`.

## Automatic GitHub builds and Releases
Every push to `main` builds an installable debug APK and publishes a GitHub Release named `Limbus FPS Unlocker Build #N`, with `LimbusFPSUnlocker.apk` attached.

The workflow requires repository Actions to be enabled and `contents: write` permission. Debug APKs are signed by the GitHub runner's debug key.

## Technical details
- Application ID: `dev.limbus.fpsunlocker`
- Limbus package checked: `com.ProjectMoon.LimbusCompany`
- Compile SDK: 35
- Java/Kotlin bytecode target: 17
- Shizuku API: 13.1.5

## References
- Shizuku API guide: https://github.com/RikkaApps/Shizuku-API
- Android FPS throttling: https://developer.android.com/games/optimize/adpf/gamemode/fps-throttling
