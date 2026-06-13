# Changelog

All notable changes to Folio are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/), and the project follows semantic
versioning.

## [Unreleased]

## [0.6.1] - 2026-06-13

### Fixed
- Reproducible builds: drop the `jvmToolchain(17)` pin added in 0.6.0. F-Droid's buildserver runs offline with Gradle toolchain auto-provisioning disabled, so that pin made the from-source build fail during configuration. The existing `compileOptions`/`kotlinOptions` already target Java 17. No user-facing changes.

## [0.6.0] - 2026-06-13

### Changed
- Reproducible-build hardening: pin the JDK toolchain to 17 so release builds are byte-for-byte identical across environments — a prerequisite for F-Droid's reproducible, developer-signed builds.

## [0.5.0] - 2026-06-13

### Added
- Appearance switcher: nine selectable looks (Charcoal, Evergreen, Slate, Paper, Ocean, Plum, Clay, Indigo, Wine), each with an in-app theme accent and a matching launcher icon, alongside light/dark and Material You.
- Search recent articles by title, feed, or text.

## [0.4.0] - 2026-06-13

### Added
- Reader mode: a Safari-style toggle in the reader that fetches the original article page and shows the clean, full-text content (with images) instead of the feed's summary — useful for feeds that only publish a teaser.

## [0.3.0] - 2026-06-13

### Added
- Background sync and offline-resilient read/star: changes are queued locally and sync automatically when the connection returns (WorkManager).
- Unread count badge on the Unread tab.
- Settings screen: account info, appearance (System/Light/Dark theme + Material You), version, and sign out.

## [0.2.0] - 2026-06-13

### Added
- Sign in to a FreshRSS server with Google Reader API credentials; session persists and can be signed out.
- Browse subscriptions and categories with unread counts and feed icons.
- Navigate across All, Unread, Starred, by category, and by feed.
- Article list with titles, feed, time, excerpt, thumbnail, and read/unread state; pull-to-refresh and paging.
- Reader view: article HTML rendered to Compose with inline images, swipe to next/previous, star, mark read/unread, share, and open in browser.
- Mark read/unread and star, written through to the server.

## [0.1.0] - 2026-06-13

### Added
- Initial project scaffold: Jetpack Compose + Material 3, Hilt, Retrofit/OkHttp/Gson,
  Coil, DataStore, single-activity MVVM structure.
- Charcoal app icon (adaptive, with a themed-icon monochrome layer) and the default
  Charcoal Material 3 theme (light and dark).
- Release pipeline: tag-driven GitHub Actions build with signing degradation, plus
  F-Droid reproducibility settings (DependencyInfoBlock disabled, baseline-profile
  generation off, R8 pinned).
