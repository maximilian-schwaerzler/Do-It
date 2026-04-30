# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run JVM unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run lint checks
./gradlew check                  # Run all checks (lint + tests)
```

To run a single test class:
```bash
./gradlew test --tests "at.co.schwaerzler.maximilian.doit.ExampleUnitTest"
```

## Project Overview

"DoIt" is an Android to-do app. As of project creation, only the template scaffold exists — no application logic has been written yet.

## Tech Stack

- **Language:** Kotlin 2.2.10
- **UI:** Jetpack Compose (BOM 2026.02.01) with Material Design 3
- **Min SDK:** 24 (Android 7.0+), **Target/Compile SDK:** 36
- **Java compatibility:** VERSION_11
- **Build system:** Gradle with Kotlin DSL (`build.gradle.kts`) and version catalog (`gradle/libs.versions.toml`)
- **Testing:** JUnit 4 (unit), Espresso + AndroidJUnit4 (instrumented), Compose UI Test

## Module Structure

Single-module project (`:app`). Package root: `at.co.schwaerzler.maximilian.doit`.

Current source files:
- `MainActivity.kt` — single `ComponentActivity` entry point, sets Compose content
- `ui/theme/` — `Color.kt`, `Theme.kt`, `Type.kt` (Material3 theme)

## Architecture Notes

No architecture has been established yet. When building out the app, follow standard Android patterns:
- **MVVM** with `ViewModel` from `androidx.lifecycle`
- **Unidirectional data flow** using Compose `State`/`StateFlow`
- Persistence via **Room** if local storage is needed
- **Hilt** for dependency injection if the app grows beyond trivial complexity
- **Navigation Compose** for multi-screen navigation

All new dependencies should be added to `gradle/libs.versions.toml` first, then referenced in `app/build.gradle.kts` via the version catalog alias.