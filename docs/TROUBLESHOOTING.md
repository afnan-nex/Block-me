# Troubleshooting Guide

## 1. Overlay Doesn't Appear

**Symptoms:** The lockdown overlay is not shown when you start a session.

**Solutions:**
1. Go to **Settings → Apps → Block me → Permissions → Display over other apps** and enable it
2. Restart the app after granting the permission
3. On MIUI (Xiaomi): Settings → Apps → Manage apps → Block me → Other permissions → Show on Lock screen → Allow

---

## 2. Screen Doesn't Lock When Pressing Home/Back

**Symptoms:** Pressing Home or Back during a session doesn't lock the screen.

**Solutions:**
1. Verify the **Accessibility Service** is enabled: Settings → Accessibility → Installed services → Block me → ON
2. Verify **Device Admin** is enabled: Settings → Security → Device admin apps → Block me → Check it's active
3. On Android 13+ sideloaded apps: Settings → Apps → Block me → three-dot menu → "Allow restricted settings", then re-enable Accessibility

---

## 3. Timer Stops in Background (Service Killed)

**Symptoms:** The timer stops counting when you lock the screen or switch apps.

**Solutions:**
1. Grant **Ignore Battery Optimizations**: Settings → Battery → Battery optimization → All apps → Block me → Don't optimize
2. **OEM-specific steps:**

   **Xiaomi/MIUI:**
   - Settings → Apps → Manage apps → Block me → Battery saver → No restrictions
   - Settings → Security → Permissions → Autostart → Block me → Enable

   **Samsung:**
   - Settings → Battery → Background usage limits → Never sleeping apps → Add Block me
   - Settings → Apps → Block me → Battery → Unrestricted

   **OnePlus/OxygenOS:**
   - Settings → Battery → Battery optimization → Block me → Don't optimize
   - Settings → Apps → Block me → Battery → Allow background activity

   **Huawei/EMUI:**
   - Settings → Battery → App launch → Block me → Manage manually → All enabled

---

## 4. Overlay Covers Status Bar

**Symptoms:** The status bar is hidden under the overlay.

**Solutions:**
- This should not happen in normal operation. If it does, please file a bug report with your Android version and device model.
- Workaround: Tap the area where the status bar should be — it may still be accessible.

---

## 5. Notification Can Be Dismissed

**Symptoms:** The session notification can be swiped away.

**Note:** By design, the persistent notification should not be dismissible while a session is active. If it is, check that the Foreground Service is running (Settings → Developer options → Running services).

---

## 6. App Doesn't Resume After Reboot

**Symptoms:** After rebooting the device during an active session, the overlay doesn't reappear.

**Solutions:**
1. Ensure the app has **BOOT_COMPLETED** permission (it should by default)
2. Some OEMs require the app to be "whitelisted" for autostart — see OEM-specific steps above
3. On Xiaomi: Settings → Security → Permissions → Autostart → Block me → Enable

---

## 7. Accessibility Service Gets Disabled

**Symptoms:** The Accessibility Service is disabled after a system update or battery optimization.

**Solutions:**
- This can happen after system updates on some OEMs
- Re-enable from Settings → Accessibility → Block me → ON
- Some OEMs disable accessibility services after reboots — the app will notify you via the permission gate

---

## 8. Phone Button Not Working

**Symptoms:** Tapping the Phone button on the overlay doesn't open the dialer.

**Solutions:**
1. Ensure you have a default phone app set: Settings → Apps → Default apps → Phone app
2. Verify the `DIAL` intent is not blocked by any security software

---

## 9. Permission Gate Always Shows

**Symptoms:** The permission setup screen appears on every launch even though permissions seem granted.

**Solutions:**
1. Go through each permission row and verify the status shows "Granted"
2. The most common issue is the **Accessibility Service** — it may show as enabled in our screen but be disabled in the system
3. Tap each row to go directly to the relevant system setting and verify
