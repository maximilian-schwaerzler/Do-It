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

"DoIt" is an Android to-do app with a Room-backed todo list. Core screens (home, add/edit todo, settings) are fully implemented.

## Tech Stack

- **Language:** Kotlin 2.3.21
- **UI:** Jetpack Compose (BOM 2026.05.00) with Material Design 3
- **Navigation:** Navigation Compose 2.9.8 with type-safe routes (`kotlinx-serialization-json`)
- **Serialization:** `kotlinx-serialization-json` 1.11.0
- **Persistence:** Room 2.8.4 (via KSP 2.3.7)
- **Date/Time:** `kotlinx-datetime` 0.8.0 (`kotlin.time.Instant` for timestamps)
- **Async:** Kotlin Coroutines + `StateFlow` / `Flow`
- **Min SDK:** 24 (Android 7.0+), **Target/Compile SDK:** 36
- **Java compatibility:** VERSION_11
- **Build system:** Gradle with Kotlin DSL (`build.gradle.kts`) and version catalog (`gradle/libs.versions.toml`)
- **Testing:** JUnit 4 (unit), Espresso + AndroidJUnit4 (instrumented), Compose UI Test

## Module Structure

Single-module project (`:app`). Package root: `at.co.schwaerzler.maximilian.doit`.

Current source files:
- `DoItApplication.kt` — custom `Application` subclass, lazily initializes the Room database
- `MainActivity.kt` — single `ComponentActivity` entry point, sets Compose content
- `data/HomeViewModel.kt` — ViewModel for home screen; exposes open/done todo lists via `Flow`
- `data/EditTodoViewModel.kt` — ViewModel for add/edit screen; manages form state and `isModified` flag
- `data/db/TodoDatabase.kt` — Room database (singleton via `getDatabase`)
- `data/db/dao/TodoDao.kt` — Room DAO with CRUD and state-update queries
- `data/db/entity/Todo.kt` — `@Entity` with `TodoState` enum (`OPEN`, `IN_PROGRESS`, `DONE`)
- `data/db/entity/TodoSummary.kt` — lightweight projection used in list views
- `data/db/Converters.kt` — Room `TypeConverter` for `kotlin.time.Instant`
- `ui/theme/` — `Color.kt`, `Theme.kt` (Material3 theme)
- `ui/component/MaxWidthLayout.kt` — layout helper that constrains width on large screens
- `ui/component/TodoListItem.kt` — reusable list item composable
- `ui/navigation/AppNavigation.kt` — `NavHost` with all type-safe routes
- `ui/navigation/screen/HomeScreen.kt` — todo list screen
- `ui/navigation/screen/EditTodoScreen.kt` — add/edit screen, reused for both modes via nullable `todoId`
- `ui/navigation/screen/SettingsScreen.kt` — settings screen (app version, GitHub, F-Droid links)
- `util/IntentUtils.kt` — extension to open URLs via `Intent`

## Architecture Notes

Follow standard Android patterns:
- **MVVM** with `ViewModel` from `androidx.lifecycle`
- **Unidirectional data flow** using Compose `StateFlow`/`Flow` collected via `collectAsStateWithLifecycle`
- **Room** for local persistence (already wired up via `DoItApplication`)
- **No Hilt** — ViewModels use `ViewModelProvider.Factory` (companion `Factory` property pattern); keep this approach unless complexity grows significantly
- **Navigation Compose** with type-safe `@Serializable` route objects

### Navigation conventions
- Define routes as `@Serializable` objects or data classes in `AppNavigation.kt`
- Pass navigation actions to screens as **callbacks** (e.g. `onAddTodo: () -> Unit`), never pass `NavController` into composables
- `EditTodoScreen` handles both add (`todoId = null`) and edit (`todoId = "<id>"`) modes via a single nullable parameter

All new dependencies should be added to `gradle/libs.versions.toml` first, then referenced in `app/build.gradle.kts` via the version catalog alias.

## Design

Whenever possible, try to stick to the [Material Design 3](https://m3.material.io/) guidelines. Use their components wherever possible.