# Release Checklist

Follow these steps in order when cutting a new release.

## 1. Pre-release checks

- [ ] All intended commits are merged to `master`
- [ ] `./gradlew check` passes clean (lint + unit tests)
- [ ] Open Weblate PRs with translation updates are merged

## 2. Version bump

Update the version in **both** of the following files:

| File                                 | Field(s) to change                                                       |
|--------------------------------------|--------------------------------------------------------------------------|
| `app/build.gradle.kts` (lines 61–62) | Increment `versionCode` by 1; set `versionName` to the new semver string |
| `CITATION.cff` (line 26)             | Set `version:` to match the new `versionName`                            |

## 3. Changelogs

Create `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` with the English release notes.

Other languages will be translated via Weblate after the release.

## 4. Commit & tag

```bash
git add app/build.gradle.kts CITATION.cff fastlane/
git commit -m "chore: release v<versionName>"
git tag v<versionName>
git push && git push --tags
```

## 5. Build the release APK

Use the **F-Droid Build** run configuration in Android Studio, or:

```bash
docker compose run --rm fdroid-build
```

This produces a reproducible build using the F-Droid buildserver Docker image.
Verify the output at `app/build/outputs/apk/release/Do-It-release.apk`.

## 6. GitHub Release

- [ ] Create a GitHub Release for the new tag (`v<versionName>`)
- [ ] Paste the `en-US` changelog as the release description
- [ ] Attach `Do-It-release.apk`

## 7. Post-release

- [ ] F-Droid detects the new tag automatically via fastlane metadata — monitor the [F-Droid build status](https://monitor.f-droid.org/builds) page
- [ ] Confirm the new version appears on F-Droid (typically takes a few days)
