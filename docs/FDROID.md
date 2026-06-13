# F-Droid submission notes

Folio is built to be F-Droid-compatible from day one. This file records the state and the plan. **MR opened (with Alan's go-ahead) on 2026-06-13:** [fdroiddata!40392](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/40392) — `New app: Folio`, Path B (developer-signed, reproducible), targeting v0.6.1. Now awaiting F-Droid maintainer review.

## Prerequisites (met)

- GPL-3.0 (full text in `LICENSE`).
- No proprietary dependencies (no Play Services, Firebase, analytics, trackers); dependency licences are noted in `app/build.gradle.kts`.
- `dependenciesInfo { includeInApk = false; includeInBundle = false }` — drops AGP's encrypted dependency-metadata signing block that F-Droid rejects.
- Release builds from source without secrets (signing degrades gracefully).
- fastlane metadata at `fastlane/metadata/android/en-US/` (title, descriptions, changelogs, 512px icon, phone screenshots — neutral content).

## Reproducibility — Path B (developer-signed) confirmed on F-Droid CI

Reproducible-build best-practices are in place (see the build config): R8 pinned to 8.8.34, ART/StartupProfile generation disabled, ServiceLoader keep-rules, and Java 17 pinned via `compileOptions`/`kotlinOptions`.

**Do not** add `kotlin { jvmToolchain(17) }`. It was added in 0.6.0 for reproducibility but broke F-Droid's from-source build: the buildserver runs offline with Gradle toolchain auto-provisioning disabled, so `jvmToolchain()` can't resolve a JDK and `fdroid build` dies during configuration (`No locally installed toolchains match ...`). The fork CI caught this on 0.6.0 — `fdroid build` failed at 1m4s while all 9 validation jobs passed. **v0.6.1 removed the block** (commit `9dac2e9`); the build then compiles, and two clean local builds (JDK 17) stay byte-for-byte identical, so determinism is unaffected.

Path B (developer-signed) lets users cross-grade between the GitHub-Releases APK and the F-Droid build, and requires the from-source build to byte-match the published APK. **Confirmed for v0.6.1** on the fork CI: `fdroid build` succeeded (2m41s) and the from-source APK verified against the published `com.folio.reader_7.apk` (`...successfully verified`), with `check apk` green. The same pipeline reran on the upstream MR.

Signing certificate SHA-256: `51f8a3a0e3452725be7323158c15f1c33429b3f607683ee310c9da40cca00056` (verified on the published v0.6.1 APK).

## Metadata (`metadata/com.folio.reader.yml`, Path B, targeting v0.6.1)

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
  - versionName: 0.6.1
    versionCode: 7
    commit: 9dac2e9cc9f2399eedb5e8a72411b39ed4f17f13
    subdir: app
    gradle:
      - yes

AllowedAPKSigningKeys: 51f8a3a0e3452725be7323158c15f1c33429b3f607683ee310c9da40cca00056

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.6.1
CurrentVersionCode: 7
```

(v0.6.0 is intentionally omitted from `Builds` — it can't build on F-Droid, see above.)

Summary/Description are NOT in the yml — F-Droid reads them from the in-repo fastlane metadata.

## Merge request (gated on Alan — hand-written, no generated text)

The MR uses the `App inclusion` template; title `New app: Folio`. Factual description to adapt by hand:

> Folio is an open-source Android reader for self-hosted FreshRSS servers, over the Google Reader API. GPL-3.0-only, no proprietary dependencies, no trackers. Release builds from source without secrets and `dependenciesInfo` is disabled. Submitting developer-signed (Path B) with `Binaries` + `AllowedAPKSigningKeys`, having confirmed the from-source build reproduces the published APK on a fdroiddata fork's `check apk`.

(Adjust the reproducibility claim to match the actual `check apk` outcome — if it failed, this becomes a plain Path A submission with no `Binaries`/`AllowedAPKSigningKeys`.)

### Steps

1. **Done — fork CI:** v0.6.1 yml pushed to the `lahuangao/fdroiddata` fork branch `add-folio`; `fdroid build` + reproducibility verification passed (0.6.0 had failed at config due to `jvmToolchain`, fixed in 0.6.1).
2. **Done — Path B kept:** `check apk` passed, so `Binaries` + `AllowedAPKSigningKeys` stay in the yml.
3. **Done — MR opened:** [fdroiddata!40392](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/40392), App-inclusion template filled by hand (no generated text), `allow_collaboration` on so maintainers can rebase. Modelled on Ultrasonic (`org.moire.ultrasonic`).

### What's next (after merge)

- The F-Droid queue is long and volunteer-run; replies should stay terse and technical. A maintainer may rebase the branch onto current master — that's normal. Don't over-trigger the issue bot.
- Once merged, `AutoUpdateMode: Version` + `UpdateCheckMode: Tags` means the F-Droid bot auto-commits routine version bumps within ~24h. The in-repo yml copy here is just a mirror; sync it from `https://gitlab.com/fdroid/fdroiddata/-/raw/master/metadata/com.folio.reader.yml` when needed.
- A future GitHub release (e.g. when adding store badges) can add the "Get it on F-Droid" badge to the README **only once the app is actually live** on f-droid.org.
