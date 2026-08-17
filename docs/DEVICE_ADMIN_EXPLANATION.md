# Device Admin Explanation

## Why Does Block me Need Device Administrator Permission?

Block me's core feature — locking the screen immediately when you try to navigate away — requires **Device Administrator** access.

---

## What Device Admin Allows Block me to Do

Block me only uses Device Admin for **one specific capability:**

> `DevicePolicyManager.lockNow()` — immediately lock the device screen

This is called when you press Home, Back, or Recent Apps during an active focus session.

---

## What Block me Does NOT Use Device Admin For

Block me explicitly does **not** use any of the following Device Admin capabilities, even though some are declared in the policy file:

- ❌ Remote wipe
- ❌ Camera disable
- ❌ Password enforcement
- ❌ Encryption requirements
- ❌ Screen timeout policies
- ❌ Monitoring login failures (though declared, this is not actively used)

The Device Admin policy file only declares:
```xml
<force-lock />    ← The only one actually used
<limit-password /> ← Declared but not actively used
<watch-login />    ← Declared but not actively used
```

---

## Is This Safe?

Yes. Device Admin is a standard Android permission that many productivity and parental control apps use. It does not give Block me access to your data, files, accounts, or messages.

Unlike Mobile Device Management (MDM) systems, Block me's Device Admin usage is local — it only triggers the screen lock action on your own device.

---

## Source Code Transparency

The Device Admin usage is fully visible in:

- `app/src/main/res/xml/device_admin_policies.xml` — the declared policies
- `app/src/main/java/com/blockme/app/util/ScreenLockManager.kt` — the only place `lockNow()` is called
- `app/src/main/java/com/blockme/app/service/LockdownDeviceAdminReceiver.kt` — the receiver callbacks

Block me is GPL-3.0 open source. You can verify every use of Device Admin in the codebase.

---

## Revoking Device Admin

You can revoke Device Admin at any time:
1. Settings → Security → Device admin apps
2. Tap Block me → Deactivate

Note: If you revoke Device Admin during an active focus session, the screen lock feature will stop working. The overlay will still show, but pressing Home/Back will no longer lock the screen.
