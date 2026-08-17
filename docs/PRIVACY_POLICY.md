# Privacy Policy — Block me

**Effective date:** 2024-01-01  
**Last updated:** 2024-01-01

---

## Summary

> **We don't collect anything, period.**

Block me is a 100% offline, privacy-first focus timer. We have designed the app to operate entirely without any server communication, account creation, or data collection of any kind.

---

## What Data We Collect

**Nothing.** We do not collect, transmit, store on external servers, or share any personal data.

---

## Data Stored On Your Device

Block me stores the following data **locally on your device only**:

- Focus session history (start time, end time, duration, goal text)
- Saved focus goals (text only)
- Recurring schedule configurations
- Usage statistics (session counts, streaks, temptation counts)
- App preferences (haptic, AMOLED mode, etc.)

This data never leaves your device unless you explicitly choose to export it.

---

## Network Access

Block me does not make any network requests. There are no:
- Analytics endpoints
- Crash reporting services
- Cloud sync servers
- Advertising networks
- Third-party SDKs that phone home

You can verify this by inspecting the source code at [GitHub](https://github.com/blockme-app/block-me) or using a network monitoring tool.

---

## Permissions Explained

| Permission | Why We Need It |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Show the lockdown overlay above other apps |
| Accessibility Service | Detect navigation button presses to enforce lockdown |
| Device Admin | Lock the screen when a blocked action is detected |
| `POST_NOTIFICATIONS` | Show the active session notification |
| `RECEIVE_BOOT_COMPLETED` | Resume active session after device reboot |
| `SCHEDULE_EXACT_ALARM` | Trigger scheduled focus sessions at exact times |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep the timer running reliably in background |
| `VIBRATE` | Haptic feedback on blocked actions |

We do not request any permissions related to contacts, location, microphone, camera (except optionally for QR unlock challenges), or any data that could identify you.

---

## Your Rights

Since all your data is stored locally, you have full control:
- **View** your data in the app's Stats screen
- **Export** your data as JSON/CSV from Settings
- **Delete** all data from Settings → Export → Delete All

---

## Children's Privacy

Block me is not directed at children under 13. We do not knowingly collect data from children.

---

## Changes to This Policy

If we make material changes to this policy, we will update the version date above and publish a new version on our GitHub repository.

---

## Contact

File an issue on GitHub: https://github.com/blockme-app/block-me/issues

---

*Block me is open source. The source code is available for independent audit at any time.*
