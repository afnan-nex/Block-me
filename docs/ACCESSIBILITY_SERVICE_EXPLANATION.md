# Accessibility Service Explanation

## Why Does Block me Need an Accessibility Service?

Block me uses Android's Accessibility Service to implement its core lockdown feature: **detecting when you press the Home, Back, or Recent Apps button during a focus session.**

Without this capability, you could simply press Home to leave the focus session and use any other app — defeating the entire purpose.

---

## What the Accessibility Service Does

**Block me's Accessibility Service ONLY:**

1. **Detects navigation button presses** (Home, Back, Recent Apps) during an active focus session
2. **Detects certain system UI transitions** such as:
   - Opening Quick Settings panel
   - Opening the App Info page for Block me in Settings
   - Opening Accessibility Settings (to prevent disabling the service mid-session)
3. **Locks the screen immediately** when a blocked action is detected

---

## What the Accessibility Service Does NOT Do

- ❌ Read text content from any app
- ❌ Capture screenshots or screen content
- ❌ Record your activity
- ❌ Transmit any data anywhere
- ❌ Access your contacts, messages, or any personal data
- ❌ Monitor your activity when no focus session is active

---

## Is This Secure?

Yes. The Accessibility Service checks `isSessionActive` before taking any action. When you are not in a focus session, the service takes no action on any accessibility events.

---

## Source Code Transparency

Block me is fully open source. You can read the exact implementation of the Accessibility Service at:

`app/src/main/java/com/blockme/app/service/LockdownAccessibilityService.kt`

The code is licensed under the MIT License, meaning anyone can audit, fork, and verify it.

---

## For Google Play Reviewers

Block me uses the Accessibility Service **exclusively** for:

> Detecting system navigation gestures (Home, Back, Recent Apps) to immediately lock the device screen when a user-initiated focus session is active, as part of a digital wellbeing lockdown tool.

This is a direct and core accessibility use case that cannot be achieved with any other Android API. The service does not read, interpret, or transmit any content from any application.
