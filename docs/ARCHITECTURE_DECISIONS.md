# Architecture Decision Records (ADRs)

## ADR-001: Why SYSTEM_ALERT_WINDOW Instead of a Full-Screen Activity

**Date:** 2024-01-01  
**Status:** Accepted

### Context
We need a persistent overlay that appears above all apps and returns immediately after unlock, without any gap showing the home screen.

### Decision
Use `WindowManager` with `TYPE_APPLICATION_OVERLAY` (aka `SYSTEM_ALERT_WINDOW`) instead of a full-screen Activity.

### Rationale
- An Activity that launches on top of others can have a brief launch gap where other apps are visible
- A `WindowManager` overlay is already registered and visible immediately on `USER_PRESENT` broadcast
- The overlay can be managed by a `Service`, which is more resilient than an Activity
- This matches the pattern used by floating timers, launcher overlays, etc.

### Consequences
- Requires `SYSTEM_ALERT_WINDOW` permission (user must grant manually via Settings)
- ComposeView inside a Service requires manual Lifecycle management

---

## ADR-002: Status Bar Inset Strategy

**Date:** 2024-01-01  
**Status:** Accepted

### Context
The overlay must not cover the status bar. Using incorrect window flags can cause the overlay to draw behind or over the status bar.

### Decision
- Do NOT use `FLAG_LAYOUT_NO_LIMITS`
- Do NOT use `FLAG_LAYOUT_IN_OVERSCAN`
- Use `WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN` with `softInputMode = SOFT_INPUT_ADJUST_RESIZE`
- Inside the Compose content, use `WindowInsets.statusBars.asPaddingValues()` as top padding

### Rationale
This combination ensures the system automatically positions the window in the usable display area, and the Compose insets API adds explicit top padding equal to the status bar height.

### Consequences
Status bar remains fully visible and accessible. Notification shade can be pulled down normally.

---

## ADR-003: Wall-Clock Timer vs. Elapsed Time

**Date:** 2024-01-01  
**Status:** Accepted

### Context
A simple elapsed-time approach fails when the app is killed, the screen turns off, or the device reboots.

### Decision
Store the **absolute end timestamp** (`endTime = startTime + durationMs`) in DataStore. Calculate remaining time as `endTime - System.currentTimeMillis()`.

### Rationale
- Survives app process death, screen off/on, and device reboots
- A 1-hour timer started at 1:00 PM will always end at 2:00 PM, regardless of interruptions
- Simple to implement and test

### Consequences
- Timer accuracy depends on device clock not being manually adjusted during session
- If device clock is set backward, the timer may appear to run slow (documented limitation)

---

## ADR-004: AccessibilityService vs. KeyEvent on Android 10+

**Date:** 2024-01-01  
**Status:** Accepted

### Context
On Android 10+, gesture navigation replaces the back/home/recents buttons. Key event interception via `onKeyEvent` may not work for gesture-based navigation.

### Decision
Use a combination of:
1. `onKeyEvent` in AccessibilityService for button-based navigation
2. `TYPE_WINDOW_STATE_CHANGED` events to detect when system UI (launcher, recents) becomes foreground

### Rationale
No single approach covers all navigation modes and OEMs. The dual approach provides maximum coverage.

### Consequences
Gesture navigation interception is best-effort. Some gesture-based navigation escapes may not be caught on all devices.

---

## ADR-005: Why DevicePolicyManager.lockNow() (Device Admin)

**Date:** 2024-01-01  
**Status:** Accepted

### Context
We need to lock the screen programmatically and immediately when a blocked action is detected.

### Decision
Use `DevicePolicyManager.lockNow()` with a registered `DeviceAdminReceiver`.

### Rationale
- This is the standard, non-root API for programmatic screen lock
- No alternative API exists for this without root
- Device Admin is clearly presented to the user during onboarding

### Consequences
- User must explicitly enable Device Admin (Device Administrator status)
- Documented prominently in onboarding as "Required / Must"
- If user revokes Device Admin, screen lock feature stops working

---

## ADR-006: Multi-Module Architecture

**Date:** 2024-01-01  
**Status:** Accepted

### Context
The app has complex features that benefit from clear separation of concerns.

### Decision
Multi-module structure: `:app`, `:core:*`, `:feature:*`

### Rationale
- Improves build times via Gradle module-level incremental compilation
- Enforces dependency rules (features cannot import each other directly)
- Matches Android's recommended modularization patterns
- Makes it easier for contributors to work on isolated features

### Consequences
More `build.gradle.kts` files to maintain. Worth the tradeoff for long-term maintainability.
