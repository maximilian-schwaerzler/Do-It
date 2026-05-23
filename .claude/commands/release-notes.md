Generate user-facing release notes for the next release by summarizing git commits since the last release tag.

## Steps

1. Find the last release tag and current versionCode:
   - Run `git tag --sort=-version:refname | head -1` to get the last tag
   - Read `versionCode` from `app/build.gradle.kts`

2. Get all commits since the last tag:
   - Run `git log <last_tag>..HEAD --oneline`

3. Analyze and filter commits:
   - **Skip:** merge commits, "Translated using Weblate" commits, `chore:` commits, `refactor:` commits, `docs:` commits, `ci:` commits, `debug:` commits
   - **Include:** `feat:` commits (new features), `fix:` commits that affect users, and any other commit that changes user-visible behavior
   - Rewrite each included commit as a plain-language bullet point (no technical jargon, no commit hash, no conventional-commit prefix)
   - If there are minor fixes not worth individual bullets, group them as `Various bug fixes and improvements`

4. Write the output to `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
   - Match the style of existing changelogs (simple `- ` bullet points, one per line)
   - Do not add a trailing newline beyond what the existing files use

5. Show me the generated changelog and confirm the file was written.

## Changelog style reference

```
- Add home screen widget to see your open todos at a glance
- Overdue todos are now highlighted in red
- Fixed dark mode not following the system setting
- Various bug fixes and improvements
```
