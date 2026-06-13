# F-Droid submission notes

Folio is built to be F-Droid-compatible from day one. This file records the state and the plan. **No merge request has been opened, and nothing has been pushed to fdroiddata** — submission is gated on the maintainer's explicit go-ahead.

## Prerequisites (met)

- GPL-3.0 (full text in `LICENSE`).
- No proprietary dependencies (no Play Services, Firebase, analytics, trackers); dependency licences are noted in `app/build.gradle.kts`.
- `dependenciesInfo { includeInApk = false; includeInBundle = false }` — drops AGP's encrypted dependency-metadata signing block that F-Droid rejects.
- Release builds from source without secrets (signing degrades gracefully).
- fastlane metadata at `fastlane/metadata/android/en-US/` (title, descriptions, changelogs, 512px icon, phone screenshots — neutral content).

## Reproducibility — confirmed locally; Path B (developer-signed) chosen

Reproducible-build best-practices are all in place (see the build config): R8 pinned to 8.8.34, ART/StartupProfile generation disabled, ServiceLoader keep-rules, and the **JDK toolchain pinned to 17** (`jvmToolchain(17)`).

Verified for **v0.6.0**:
- Two clean local builds are byte-for-byte identical (deterministic) — `./gradlew clean assembleRelease --no-build-cache` ×2, diff excluding `META-INF`.
- The published GitHub-CI APK and a local build of the same tag are byte-for-byte identical (`classes.dex`, `resources.arsc`, `AndroidManifest.xml` all match).

So Folio looks reproducible, which makes the developer-signed reproducible path (Path B) viable — users can cross-grade between the GitHub APK and the F-Droid build. The remaining proof is `fdroid build` + `check apk` on a fdroiddata **fork's** CI, whose environment differs again; run that before opening the real MR. If `check apk` fails, drop `Binaries` + `AllowedAPKSigningKeys` (Path A).

Signing certificate SHA-256: `51f8a3a0e3452725be7323158c15f1c33429b3f607683ee310c9da40cca00056`

## Metadata (`metadata/com.folio.reader.yml`, Path B, targeting v0.6.0)

```yaml
Categories:
  - Internet
  - Reading
License: GPL-3.0-only
AuthorName: Alan Huang
SourceCode: https://github.com/AlanHuang99/Folio
IssueTracker: https://github.com/AlanHuang99/Folio/issues

AutoName: Folio

RepoType: git
Repo: https://github.com/AlanHuang99/Folio.git

Binaries: https://github.com/AlanHuang99/Folio/releases/download/v%v/folio-v%v.apk

Builds:
  - versionName: 0.6.0
    versionCode: 6
    commit: 83bae4d301c4879dc03d51240a9e9b2b09082ccf
    subdir: app
    gradle:
      - yes

AllowedAPKSigningKeys: 51f8a3a0e3452725be7323158c15f1c33429b3f607683ee310c9da40cca00056

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.6.0
CurrentVersionCode: 6
```

Summary/Description are NOT in the yml — F-Droid reads them from the in-repo fastlane metadata.

## Merge request (gated on Alan — hand-written, no generated text)

The MR uses the `App inclusion` template; title `New app: Folio`. Factual description to adapt by hand:

> Folio is an open-source Android reader for self-hosted FreshRSS servers, over the Google Reader API. GPL-3.0-only, no proprietary dependencies, no trackers. Release builds from source without secrets, `dependenciesInfo` is disabled, and the build is reproducible (verified: two-build diff and published-vs-local are byte-for-byte identical), so this is submitted developer-signed (Path B) with `Binaries` + `AllowedAPKSigningKeys`.

### Steps (all awaiting Alan's go-ahead)

1. Push the yml above to a branch on the `lahuangao/fdroiddata` fork; let the fork CI run `fdroid build` + `check apk` to confirm v0.6.0 reproduces in F-Droid's environment.
2. If `check apk` passes, keep Path B; if it fails with a digest mismatch, drop `Binaries` + `AllowedAPKSigningKeys` (Path A).
3. Open `New app: Folio` against fdroiddata, filling the App-inclusion template by hand. Reference app to model: Ultrasonic (`org.moire.ultrasonic`).
