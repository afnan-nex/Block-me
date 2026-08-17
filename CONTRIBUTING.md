# Contributing to Block me

Thank you for your interest in contributing! Block me is a community-driven, privacy-first open-source project.

---

## Code Style

- **Kotlin**: Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose**: Follow [Compose API guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md)
- **Architecture**: MVVM + Repository pattern — no business logic in Composables or Services
- **No direct DataStore/Room calls from UI layer** — always go through a ViewModel and UseCase
- **Hilt for all DI** — no manual object creation

## Commit Convention

```
feat: add unlock challenge via math problems
fix: overlay not appearing after unlock on Samsung
docs: add troubleshooting for Xiaomi MIUI
test: add streak calculation edge cases
```

## Pull Request Process

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Write tests for new functionality
4. Run `./gradlew lint test` — must pass
5. Open a PR with a clear description

## What We Won't Accept

- Network calls (core feature must be 100% offline)
- Analytics or tracking of any kind
- Features that compromise the strict no-exit policy
- Code that collects or transmits any user data

## Reporting Bugs

Use the [bug report template](.github/ISSUE_TEMPLATE/bug_report.md).

## Requesting Features

Use the [feature request template](.github/ISSUE_TEMPLATE/feature_request.md).
