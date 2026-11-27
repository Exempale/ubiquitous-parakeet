# Limbus FPS Unlocker

A Shizuku-powered GameManager configuration helper for Android.

## Current app features
- Shows the phone's current display refresh rate and supported modes.
- Checks that Limbus Company is installed and can launch it.
- Checks Shizuku availability and runtime permission.
- Runs short Android shell commands through Shizuku's `newProcess` API (no custom UserService Binder).
- Checks the GameManager modes Android reports as available for Limbus Company.
- Enables Performance mode only when Android reports it as supported, then verifies the selected mode.
- Restores Standard mode and verifies the result.
- Copies diagnostic logs to the clipboard.
- Custom neon FPS launcher icon.

## Important FPS limitation
Android's GameManager `fps` intervention is **FPS throttling**: it can cap a game to a supported value, but it cannot turn a game's own 60 FPS limit into 120 FPS. The setting does not modify Limbus Company's Unity runtime. A successful command only confirms that the system configuration was written; actual in-game FPS must be measured separately.

The supported intervention values depend on the device's display mode. For a 120 Hz display, Android documents 120/60/40/30 as supported throttle values. See the Android developer documentation linked below.

## Shizuku
1. Install and start Shizuku using Wireless debugging.
2. Open Limbus FPS Unlocker.
3. Grant the app access in Shizuku when prompted.
4. Use **Enable Performance Mode** only if Android reports that mode as supported for the game.
5. Use **Return to Standard Mode** to restore the normal mode.

The app uses Shizuku's one-shot process API. On Android 16, shell writes to `game_overlay/<package>` may be rejected because the flag is not in the OS DeviceConfig allowlist. The app therefore does not attempt to bypass that restriction or claim that setting `fps=120` unlocks the game. It uses the supported GameManager mode command and verifies the resulting mode.

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
