![Social Banner](assets/images/social-banner.png)

[![F-Droid Version](https://img.shields.io/f-droid/v/at.co.schwaerzler.maximilian.doit)](https://f-droid.org/packages/at.co.schwaerzler.maximilian.doit/)
[![GitHub Release](https://img.shields.io/github/v/release/maximilian-schwaerzler/Do-It)](https://github.com/maximilian-schwaerzler/Do-It/releases/latest)
[![Translation status](https://weblate.maximilian.schwaerzler.co.at/widget/do-it/svg-badge.svg)](https://weblate.maximilian.schwaerzler.co.at/engage/do-it/)
[![GitHub License](https://img.shields.io/github/license/maximilian-schwaerzler/Do-It)](https://github.com/maximilian-schwaerzler/Do-It/blob/master/LICENSE)

# Do-It

A modern Android to-do app built with Jetpack Compose and Material Design 3.

## Features

- View open and completed todos in separate sections
- Add and edit todos with a title, optional description, and optional deadline
- Mark todos complete or incomplete with a single tap
- Multi-select todos via long-press, then bulk-delete or select all
- Motivational empty-state messages when the list is empty or everything is done
- Edge-to-edge UI following Material Design 3 guidelines
- Full app translations into multiple languages:
    - English
    - German
    - Spanish
    - French
    - Italian
    - Korean
    - Lithuanian
    - Polish
    - Romanian
    - Serbian
    - Tamil
    - Turkish
    - Chinese (simplified), thank you, Emily, for proofreading!
    - Want to add your language? Contribute on [Weblate](https://weblate.maximilian.schwaerzler.co.at/engage/do-it/) or open
      an [issue](https://github.com/maximilian-schwaerzler/Do-It/issues/new?template=language_request.yml)!

## Tech Stack

| Layer       | Technology                                    |
|-------------|-----------------------------------------------|
| Language    | Kotlin 2.3.21                                 |
| UI          | Jetpack Compose (BOM 2026.05.00) + Material 3 |
| Navigation  | Navigation Compose 2.9.8 (type-safe routes)   |
| Persistence | Room 2.8.4 · DataStore Preferences 1.2.1      |
| Async       | Kotlinx Coroutines 1.11.0                     |
| Date/time   | Kotlinx Datetime 0.8.0                        |
| Min SDK     | 26 (Android 8.0)                              |
| Target SDK  | 36 (Android 15)                               |
| Build       | Gradle Kotlin DSL + version catalog           |

## Requirements

- Android Studio Meerkat or newer
- JDK 11+
- Android device or emulator running API 26+

## App translation

Translating the app into as many languages as possible is one of my goals. Translations are managed
on [Weblate](https://weblate.maximilian.schwaerzler.co.at/engage/do-it/) — the easiest way to
contribute is to sign up there and suggest or improve strings directly in your browser. No Git
knowledge required!

If you spot a translation that doesn't make sense, please don't be shy — open a translation on
Weblate or a [pull request](https://github.com/maximilian-schwaerzler/Do-It/pulls). **Thank you
very much!**

## Installation

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/at.co.schwaerzler.maximilian.doit)

Download the app on
the [F-Droid App Store](https://f-droid.org/packages/at.co.schwaerzler.maximilian.doit) or directly
from the [Releases](https://github.com/maximilian-schwaerzler/Do-It/releases) here.

## Getting Started

Clone the repository and open it in Android Studio, or build from the command line:

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run lint checks
```

# For reproducible builds:

Either use the `F-Droid Build` run config in IDEA-based IDEs like Android Studio or run
`docker compose run --rm fdroid-build` from the project's root directory. Make sure you have
a [Docker](https://www.docker.com/) version supporting Docker Compose installed.

## Project Structure

Single-module project (`:app`). Package root: `at.co.schwaerzler.maximilian.doit`.

```text
app/src/main/java/at/co/schwaerzler/maximilian/doit/
├── DoItApplication.kt
├── MainActivity.kt
├── OverviewWidget.kt
├── data/
│   ├── DeadlineNotificationWorker.kt
│   ├── EditTodoViewModel.kt
│   ├── HomeViewModel.kt
│   ├── SettingsViewModel.kt
│   ├── OverviewWidgetReceiver.kt
│   └── db/
│       ├── Converters.kt
│       ├── TodoDatabase.kt
│       ├── TodoRepository.kt
│       ├── dao/TodoDao.kt
│       └── entity/
│           ├── Todo.kt
│           └── TodoSummary.kt
├── ui/
│   ├── component/
│   │   ├── MaxWidthLayout.kt
│   │   ├── NotificationPermissionDialog.kt
│   │   └── TodoListItem.kt
│   ├── navigation/
│   │   ├── AppNavigation.kt
│   │   └── screen/
│   │       ├── EditTodoScreen.kt
│   │       ├── HomeScreen.kt
│   │       └── SettingsScreen.kt
│   └── theme/
│       ├── Color.kt
│       └── Theme.kt
└── util/
    ├── AppPreferences.kt
    ├── IntentUtils.kt
    ├── NotificationLeadTime.kt
    └── ThemeUtils.kt
```

## Architecture

The app follows MVVM with unidirectional data flow: screens observe `StateFlow` exposed by
ViewModels and send events back as simple function calls. Room handles local persistence via a
`TodoDao`. Navigation uses type-safe `@Serializable` route objects — screens receive navigation
actions as callbacks and never hold a reference to `NavController`. `EditTodoScreen` serves as
both the add and edit screen via a single nullable `todoId` parameter.
