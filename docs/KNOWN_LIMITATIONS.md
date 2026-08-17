# Known Limitations

Block me is a powerful focus tool, but there are inherent Android limitations that cannot be overcome without rooting the device or modifying the OS.

---

## 1. Safe Mode

**Impact:** Critical  
**Description:** Booting into Safe Mode disables all third-party apps, including Block me. The overlay will not appear, the AccessibilityService will not run, and the timer will not be enforced.  
**Workaround:** None. This is an intentional Android security feature that cannot be bypassed.  
**Recommendation:** Don't boot into Safe Mode during a focus session.

---

## 2. ADB Uninstall

**Impact:** High  
**Description:** The app can be uninstalled via ADB (`adb uninstall com.blockme.app`) without triggering any lock. This bypasses the Device Admin protection.  
**Workaround:** None. ADB has system-level access.  
**Known use:** This is intentionally left as a documented escape hatch for users who need to recover their device in genuine emergencies.

---

## 3. Factory Reset

**Impact:** High  
**Description:** A factory reset will erase the app and all session data. This is always possible.  
**Workaround:** None. Not a technical problem — a human one.

---

## 4. Android 13+ Restricted Settings (Sideloaded Apps)

**Impact:** Medium  
**Description:** Apps sideloaded outside the Google Play Store on Android 13+ may encounter "Restricted Settings" when trying to enable the Accessibility Service.  
**Workaround:** 
1. Open Settings → Apps → Block me
2. Tap the three-dot menu → "Allow restricted settings"
3. Then enable the Accessibility Service normally

---

## 5. OEM Battery Optimization (Aggressive Killing)

**Impact:** Medium  
**Description:** Some OEMs (Xiaomi/MIUI, Huawei/EMUI, OnePlus/OxygenOS, Samsung/OneUI) aggressively kill background services even when the user has granted "Ignore Battery Optimizations."

**Per-OEM instructions:**

| OEM | Setting Location |
|---|---|
| Xiaomi/MIUI | Settings → Apps → Manage apps → Block me → Battery saver → No restrictions |
| Samsung | Settings → Battery → Background usage limits → Never sleeping apps → Add Block me |
| Huawei | Settings → Battery → App launch → Block me → Manage manually → Enable all |
| OnePlus | Settings → Battery → Battery optimization → Block me → Don't optimize |

---

## 6. Google Play Accessibility Policy

**Impact:** Distribution  
**Description:** Apps using AccessibilityService face scrutiny from Google Play. The app must clearly justify the accessibility service usage in its Play Store listing.  
**Our justification:** The accessibility service is used solely to detect Home/Back/Recent Apps button presses during user-initiated lockdown sessions. It does not read app content or transmit any data.

---

## 7. Android Version Differences

**Description:** Navigation button interception behavior varies across Android versions:
- Android 10+ uses gesture navigation by default. Back gesture detection via AccessibilityService may not work identically to button-based navigation.
- Android 12+ requires `SCHEDULE_EXACT_ALARM` permission for exact alarms.
- Android 13+ requires `POST_NOTIFICATIONS` permission for notifications.

---

## 8. Split-Screen / Multi-Window on Certain OEMs

**Description:** Some OEM implementations of split-screen may not fire accessibility events that the app can detect. The lock behavior may be slightly delayed on these devices.

---

## 9. Force-Stop via Settings (Non-Root)

**Description:** If a user navigates to Settings → Apps → Block me and force-stops the app, the overlay and timer services will stop. The app detects navigation to its own App Info page and locks the screen, but a sufficiently fast user or an alternative method may bypass this.  
**Mitigation:** The app monitors for navigation to the App Info page and locks the screen immediately upon detection.
