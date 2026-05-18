# Contributing to Do It

Thank you for your interest in contributing! All kinds of contributions are welcome — bug reports,
feature ideas, translations, and code changes. Please read this guide before submitting anything.

For a general overview of the project see the [README](README.md).

---

## Ways to Contribute

| Type                      | How                                                                           |
|---------------------------|-------------------------------------------------------------------------------|
| Bug report                | Open a [GitHub issue](https://github.com/maximilian-schwaerzler/Do-It/issues) |
| Feature request           | Open a [GitHub issue](https://github.com/maximilian-schwaerzler/Do-It/issues) |
| New / updated translation | See [Translations](#translations) below                                       |
| Code change               | Fork → branch → PR (see [Pull Requests](#submitting-a-pull-request))          |

---

## Development Setup

**Prerequisites**

- [Android Studio](https://developer.android.com/studio) Meerkat or newer
- JDK 11 or newer (bundled with Android Studio)
- Android SDK with API 24 (minimum) and API 36 (target) installed

**Getting started**

```bash
git clone https://github.com/maximilian-schwaerzler/Do-It.git
```

Open the cloned directory in Android Studio. Gradle will sync automatically.

---

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

---

## Code Style

- Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html) (
  `kotlin.code.style=official` is set in `gradle.properties`).
- Follow [Material Design 3](https://m3.material.io/) guidelines; use MD3 components wherever
  possible.
- Keep the MVVM + unidirectional data flow architecture. ViewModels expose state via `StateFlow`/
  `Flow`; composables collect with `collectAsStateWithLifecycle`.
- Prefer the existing `ViewModelProvider.Factory` companion pattern over Hilt. Only introduce Hilt
  if the dependency graph becomes complex enough to justify it.
- Add all new dependencies to `gradle/libs.versions.toml` first, then reference them in
  `app/build.gradle.kts` via the version catalog alias.

---

## Commit Message Convention

Use semantic commit prefixes so the history stays easy to scan:

| Prefix      | When to use                                       |
|-------------|---------------------------------------------------|
| `feat:`     | New user-facing feature                           |
| `fix:`      | Bug fix                                           |
| `ui:`       | Visual / layout change                            |
| `i18n:`     | Translation or localization work                  |
| `anim:`     | Animation or motion change                        |
| `refactor:` | Internal restructure with no behavior change      |
| `style:`    | Code formatting or drawable/style resource change |
| `docs:`     | Documentation only                                |
| `chore:`    | Build scripts, dependencies, tooling              |

Add a scope in parentheses when it helps (`feat(settings):`, `fix(room):`). Reference related issues
at the end of the subject line where applicable (`fixes #42`).

---

## Submitting a Pull Request

1. Fork the repository and create a branch from `master`.
2. Make your changes and add or update tests as appropriate.
3. Run `./gradlew check` and make sure everything passes.
4. Open a pull request against `master`. Keep the PR focused on a single concern and link to any
   related issue.

---

## Translations

The app is localized using Android string resources under `app/src/main/res/`.

**Request a new language** — open an issue using
the [Language Request](https://github.com/maximilian-schwaerzler/Do-It/issues/new?template=language_request.yml)
template.

**Contribute or update a translation** — two helper tools are available in the `tools/` directory:

- `tools/translations-editor.html` — browser-based editor for reviewing and editing translation
  strings side-by-side.
- `tools/translate.py` — script that uses the [DeepL API](https://developers.deepl.com/api-reference/translate) to
  auto-translate missing strings. Run `python tools/translate.py --help` for usage.

After updating translations, open a pull request as described above.

---

## License

By contributing you agree that your changes will be licensed under the [Apache License 2.0](LICENSE)
that covers this project.
