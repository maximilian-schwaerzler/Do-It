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

"DoIt" is an Android to-do app. The main app navigation has been added. Some screens still need to be implemented.

## Tech Stack

- **Language:** Kotlin 2.3.21
- **UI:** Jetpack Compose (BOM 2026.04.01) with Material Design 3
- **Navigation:** Navigation Compose 2.9.8 with type-safe routes (`kotlinx-serialization-json`)
- **Serialization:** `kotlinx-serialization-json` 1.7.3
- **Min SDK:** 24 (Android 7.0+), **Target/Compile SDK:** 36
- **Java compatibility:** VERSION_11
- **Build system:** Gradle with Kotlin DSL (`build.gradle.kts`) and version catalog (`gradle/libs.versions.toml`)
- **Testing:** JUnit 4 (unit), Espresso + AndroidJUnit4 (instrumented), Compose UI Test

## Module Structure

Single-module project (`:app`). Package root: `at.co.schwaerzler.maximilian.doit`.

Current source files:
- `MainActivity.kt` — single `ComponentActivity` entry point, sets Compose content
- `ui/theme/` — `Color.kt`, `Theme.kt`, `Type.kt` (Material3 theme)
- `ui/navigation/AppNavigation.kt` — `NavHost` with all type-safe routes
- `ui/navigation/screen/HomeScreen.kt` — todo list screen (stub)
- `ui/navigation/screen/EditTodoScreen.kt` — add/edit screen (stub), reused for both modes via nullable `todoId`

## Architecture Notes

Follow standard Android patterns:
- **MVVM** with `ViewModel` from `androidx.lifecycle`
- **Unidirectional data flow** using Compose `State`/`StateFlow`
- Persistence via **Room** if local storage is needed
- **Hilt** for dependency injection if the app grows beyond trivial complexity
- **Navigation Compose** with type-safe `@Serializable` route objects

### Navigation conventions
- Define routes as `@Serializable` objects or data classes in `AppNavigation.kt`
- Pass navigation actions to screens as **callbacks** (e.g. `onAddTodo: () -> Unit`), never pass `NavController` into composables
- `EditTodoScreen` handles both add (`todoId = null`) and edit (`todoId = "<id>"`) modes via a single nullable parameter

All new dependencies should be added to `gradle/libs.versions.toml` first, then referenced in `app/build.gradle.kts` via the version catalog alias.

## Design

Whenever possible, try to stick to the [Material Design 3](https://m3.material.io/) guidelines. Use their components wherever possible.