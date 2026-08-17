# Block me 🔒

> **Your no-compromise focus companion.** Open-source Android full-phone lockdown timer for digital wellbeing.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![CI](https://github.com/blockme-app/block-me/actions/workflows/ci.yml/badge.svg)](https://github.com/blockme-app/block-me/actions)
[![Min SDK](https://img.shields.io/badge/minSdk-26-green)](https://developer.android.com/about/versions/oreo)

---

## What is Block me?

Block me completely locks your Android phone during focus sessions. When you start a session:

- A **persistent fullscreen overlay** covers the entire usable screen area (below the status bar)
- Pressing **Home, Back, or Recent Apps** immediately locks your screen — you must unlock to return to the overlay
- The **power button** works normally — screen off/on preserves the session
- **Notification shade** can be pulled down freely
- The overlay includes a **Phone button** for emergency calls

No pause. No early exit. No compromise.

---

## Features

| Feature | Status |
|---|---|
| Full-screen lockdown overlay | ✅ |
| Home/Back/Recents interception → screen lock | ✅ |
| Session persists through screen off/reboot | ✅ |
| Notification shade allowed | ✅ |
| Power button allowed | ✅ |
| Phone button for emergency calls | ✅ |
| Wall-clock based timer (not elapsed time) | ✅ |
| Maximum 3-hour session limit | ✅ |
| Custom and preset duration picker | ✅ |
| Focus goal setting | ✅ |
| Usage analytics (local only) | ✅ |
| Weekly bar chart + monthly heatmap | ✅ |
| Recurring scheduled sessions | ✅ |
| Session persistence through reboot | ✅ |
| Strict Mode (no early exit) | ✅ |
| AMOLED dark mode | ✅ |
| Haptic pulse every minute | ✅ |
| Unlock challenges | 🚧 Coming soon |
| Social accountability | 🚧 Planned |
| Zero network calls | ✅ |
| No tracking, no ads | ✅ |
| MIT open source | ✅ |

---

## Architecture

```
:app                    ← Activity, Navigation, Services, Receivers
:core:common            ← Constants, Extensions, Result
:core:data              ← Room, DataStore, Repository implementations
:core:domain            ← Models, Repository interfaces, Use cases
:core:ui                ← Theme, Composable components
:feature:permissions    ← Permission gate onboarding screen
:feature:timer          ← Timer setup screen + ViewModel
:feature:overlay        ← Lockdown overlay composable
:feature:stats          ← Statistics screen + ViewModel
:feature:schedule       ← Schedule management screen + ViewModel
:feature:settings       ← Settings screen + ViewModel
```

**Tech stack:** Kotlin, Jetpack Compose + Material3, Hilt, Room, DataStore, Coroutines + StateFlow, Navigation Compose, AlarmManager, AccessibilityService, DeviceAdminReceiver

---

## Building

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 36

### Build

```bash
git clone https://github.com/blockme-app/block-me.git
cd block-me
./gradlew assembleDebug
```

### Run tests

```bash
./gradlew test          # Unit tests
./gradlew lint          # Lint check
```

### Install on device

```bash
./gradlew installDebug
```

---

## Required Permissions

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Show fullscreen lockdown overlay |
| Accessibility Service | Detect Home/Back/Recents presses |
| Device Admin | Lock screen immediately when blocked action detected |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep timer running in background |
| `POST_NOTIFICATIONS` | Show session notification |
| `SCHEDULE_EXACT_ALARM` | Start scheduled sessions on time |
| `RECEIVE_BOOT_COMPLETED` | Restore active session after reboot |
| `FOREGROUND_SERVICE` | Run timer + overlay as foreground service |

---

## Privacy

**We don't collect anything, period.**

- Zero network calls
- All data stored locally in Room database
- No crash reporting
- No analytics
- No account required
- All data exportable as JSON

---

## Known Limitations

See [KNOWN_LIMITATIONS.md](docs/KNOWN_LIMITATIONS.md).

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

---

## License

MIT License — see [LICENSE](LICENSE).
