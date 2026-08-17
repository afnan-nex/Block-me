# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-08-17

### Added
- Status bar notification vector icon (`ic_notification.xml` / `ic_stat_name.xml`) with Material You dynamic color support.
- Material 3 analog `TimePicker` for schedule start time configuration.
- Adaptive vector launcher icon layers (`ic_launcher_background.xml`, `ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`).
- Asynchronous in-memory package caching in `LockdownAccessibilityService` for zero-lag keyboard and launcher recognition.
- Status bar bounding constraints for the floating timer popup to prevent notification shade interference.
- Enlarged 2x3 duration preset button grid (`15m, 30m, 45m` and `1h, 2h, 3h`) with bold typography.

### Changed
- Increased emergency unlock challenge tap threshold to 120 taps.
- Updated unlock challenge description to *"Tap the timer button 120 times during the session to exit early."*
- Renamed main action button from *"Start Focus Session"* to *"Start"*.
- Defaulted haptic pulse toggle to off in user preferences.
- Improved pledge typing verification to evaluate whole words on space/newline rather than interrupting per keystroke.
- Retained minute selection state when sliding duration hours to 3h by greying out the slider instead of resetting.

### Removed
- Removed redundant *"Configure your focus session"* subtitle and *"TARGET DURATION"* label from the timer setup screen.
- Removed side label text from duration sliders to allow full-width slider interaction.
- Removed *"FOCUS LOCKDOWN"* banner and *"Emergency calls only"* footer text from the overlay screen.
- Removed emojis from emergency unlock dialogs and confirmation sheets.

### Fixed
- Fixed duplicate notifications by migrating to a single foreground service architecture (`TimerForegroundService`), completely eliminating the stuck `00:00:00` notification.
- Fixed main-thread ANR ("App isn't responding / Force close") caused by synchronous Binder IPC during Volume key and Notification shade interactions.
- Whitelisted OEM volume panels, quick settings, and SystemUI components in `LockdownAccessibilityService` to prevent false lockdown triggers.
- Fixed repeated app crashes on resume (`ForegroundServiceDidNotStartInTimeException`) by ensuring `LockdownOverlayService` runs purely as a window overlay service.
- Fixed bug where the overlay remained stuck on screen at `00:00:00` after timer expiration by reacting to `isSessionActive` state changes and `ACTION_SESSION_COMPLETE` broadcasts.
- Fixed 1-second (`00:00:01`) slider edge-case by enforcing a strict 1-minute minimum limit.

## [1.0.0] - 2026-08-15

### Added
- Full-phone focus lockdown enforcing distraction-free sessions via `AccessibilityService` and `WindowManager` overlay.
- Countdown timer widget with smooth circular progress and time formatters.
- Preset and custom duration selectors supporting sessions from 1 minute up to 3 hours.
- Emergency dialer integration with floating draggable mini timer overlay.
- Emergency unlock challenge with 42-word pledge typing verification.
- Focus goal configuration and persistence with Room database and DataStore.
- Scheduled focus sessions using `AlarmManager` with automatic service launch.
- Historical focus analytics and statistics screen.
- AMOLED true black dark mode toggle and customizable settings.
- Device administrator integration for anti-tamper protection and instant screen locking.
- 100% offline architecture with zero telemetry, network requests, or third-party tracking.
