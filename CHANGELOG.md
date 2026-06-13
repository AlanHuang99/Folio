# Changelog

All notable changes to Folio are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/), and the project follows semantic
versioning.

## [Unreleased]

## [0.1.0] - 2026-06-13

### Added
- Initial project scaffold: Jetpack Compose + Material 3, Hilt, Retrofit/OkHttp/Gson,
  Coil, DataStore, single-activity MVVM structure.
- Charcoal app icon (adaptive, with a themed-icon monochrome layer) and the default
  Charcoal Material 3 theme (light and dark).
- Release pipeline: tag-driven GitHub Actions build with signing degradation, plus
  F-Droid reproducibility settings (DependencyInfoBlock disabled, baseline-profile
  generation off, R8 pinned).
