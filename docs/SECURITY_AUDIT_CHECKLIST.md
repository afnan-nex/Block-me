# Security Audit Checklist

## Network Security
- [ ] No outbound network connections (verify with `adb logcat | grep -i "http\|socket\|connect"`)
- [ ] No external SDKs with telemetry (verify in `libs.versions.toml`)
- [ ] No DNS lookups
- [ ] NetworkSecurityConfig blocks all cleartext traffic

## Data Security
- [ ] Room database uses in-app storage only
- [ ] DataStore is in app-private storage
- [ ] No content providers that expose data externally
- [ ] Exported components restricted (check `AndroidManifest.xml` for `android:exported`)

## Permissions Audit
- [ ] All declared permissions are used
- [ ] No permissions broader than needed
- [ ] Optional permissions (camera, NFC) not requested unless unlock challenge is enabled
- [ ] `INTERNET` permission is NOT declared

## Service Security
- [ ] LockdownOverlayService is not exported
- [ ] TimerForegroundService is not exported
- [ ] AlarmReceiver is not exported (only explicit intents)
- [ ] BootCompletedReceiver validates intent action before proceeding

## Accessibility Service
- [ ] Service only acts during active sessions (`isSessionActive` check)
- [ ] No content reading (no `getText()`, `findAccessibilityNodeInfosByText()` on non-Settings packages)
- [ ] `canRetrieveWindowContent` is only used to check package names

## Device Admin
- [ ] Only `force-lock`, `limit-password`, `watch-login` policies
- [ ] No remote wipe, camera disable, or other invasive policies
- [ ] Device Admin disablement is gracefully handled

## Code Integrity
- [ ] ProGuard enabled for release builds
- [ ] No debug logging in release builds
- [ ] No hardcoded secrets or keys
- [ ] Build types properly separate debug from release

## Third-Party Libraries
Review each library in `libs.versions.toml` for:
- [ ] No analytics (Firebase Analytics, Mixpanel, Amplitude, etc.)
- [ ] No crash reporting with remote upload (Crashlytics, Sentry, etc.)
- [ ] No ad SDKs

**Approved libraries:** AndroidX, Hilt, Room, DataStore, Compose, Navigation, Turbine, MockK, Coroutines. All are Google/JetBrains/trusted open-source libraries with no telemetry.
