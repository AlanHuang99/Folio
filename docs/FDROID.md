# F-Droid submission notes

Folio is built to be F-Droid-compatible from day one. This file records the state and the plan. **No merge request has been opened** — submission is gated on the maintainer's explicit go-ahead.

## Prerequisites (met)

- GPL-3.0 (full text in `LICENSE`).
- No proprietary dependencies (no Play Services, Firebase, analytics, trackers).
- `dependenciesInfo { includeInApk = false; includeInBundle = false }` — drops AGP's encrypted dependency-metadata signing block that F-Droid rejects.
- Release builds from source without secrets (signing degrades gracefully).
- Reproducibility settings: R8 pinned to 8.8.34, ART/StartupProfile generation disabled.
- fastlane metadata at `fastlane/metadata/android/en-US/` (title, descriptions, changelogs, 512px icon, phone screenshots).

## Reproducibility — verified locally (Path B viable)

A local predictor for v0.2.0 (build the tagged source locally, byte-compare against the GitHub-published APK) showed a **full match**: `classes.dex` and all 56 non-signature entries are identical. Folio therefore looks **reproducible**, which makes the developer-signed reproducible path (Path B) viable — unlike Resonance, whose Hilt/KSP DEX differed and which had to use F-Droid signing (Path A).

This is promising but not yet conclusive: the real test is `fdroid build` + `check apk` on a fdroiddata fork's CI, whose build environment differs again. **Run that before committing to Path B** (the `Binaries` + `AllowedAPKSigningKeys` choice is permanent). If `check apk` fails, fall back to Path A (no `Binaries`, no `AllowedAPKSigningKeys`).

Signing certificate SHA-256: `51f8a3a0e3452725be7323158c15f1c33429b3f607683ee310c9da40cca00056`

## Draft metadata (`metadata/com.folio.reader.yml`, Path B)

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
  - versionName: 0.2.0
    versionCode: 2
    commit: c2783e08066be26033b47a5641fdfe1c28cd7f20
    subdir: app
    gradle:
      - yes

AllowedAPKSigningKeys: 51f8a3a0e3452725be7323158c15f1c33429b3f607683ee310c9da40cca00056

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.2.0
CurrentVersionCode: 2
```

## Submission steps (gated on maintainer)

1. Push the draft yml to a `<fork>/fdroiddata` branch and let the fork CI run `fdroid build` + `check apk` to confirm reproducibility for v0.2.0.
2. If `check apk` passes, keep Path B. If it fails with a digest mismatch, drop `Binaries` + `AllowedAPKSigningKeys` (Path A).
3. Open a `New app: Folio` MR using the App-inclusion template, filled in by hand (no generated text). Summary/Description come from the in-repo fastlane metadata, not the yml.
