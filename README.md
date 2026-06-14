<div align="center">

<img src="docs/icon.png" width="96" alt="Folio icon" />

# Folio

An open-source Android reader for self-hosted [FreshRSS](https://freshrss.org/), over the Google Reader API.

[![Build](https://github.com/AlanHuang99/Folio/actions/workflows/build.yml/badge.svg)](https://github.com/AlanHuang99/Folio/actions/workflows/build.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/AlanHuang99/Folio)](https://github.com/AlanHuang99/Folio/releases/latest)

</div>

Folio connects to a FreshRSS server (or any server that speaks the Google Reader API) and lets you read, organize, and star your feeds. It is built with Jetpack Compose and Material 3, with no proprietary dependencies, no tracking, and no advertising.

## Screenshots

<p>
  <img src="docs/screenshots/1.png" width="30%" alt="Subscriptions" />
  <img src="docs/screenshots/2.png" width="30%" alt="Article list" />
  <img src="docs/screenshots/3.png" width="30%" alt="Reader" />
</p>

## Features

- Connect to a self-hosted FreshRSS server with your Google Reader API credentials
- Sign in to multiple accounts and switch between them
- Browse subscriptions and categories — All, Unread, Starred, by category, by feed — with unread counts and feed icons
- Manage feeds in the app: add a feed by URL, rename, move between folders, and unsubscribe; rename and delete folders
- Article list with title, feed, time, excerpt, thumbnail, and read/unread state; pull-to-refresh and paging
- Reader view: article HTML rendered to native text with inline images; swipe between articles; star, mark read/unread, share, open in browser
- Reader mode: fetch the original page and read the clean, full-text article
- Background sync and offline-resilient read/star — changes queue locally and sync when the connection returns
- Search recent articles by title, feed, or text
- Nine selectable looks (in-app theme + matching launcher icon), light/dark, and Material You
- Free software (GPLv3) — no proprietary dependencies, no tracking, no advertising

## Requirements

- A server that speaks the Google Reader API (for example, [FreshRSS](https://freshrss.org/))
- Android 8.0 (API 26) or newer

## Install

Download the latest APK from the [Releases](https://github.com/AlanHuang99/Folio/releases/latest) page. Publishing on F-Droid is planned.

## Build from source

Requirements: JDK 17 and the Android SDK.

```bash
git clone https://github.com/AlanHuang99/Folio.git
cd Folio
./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/
./gradlew installDebug           # install on a connected device
```

The release build compiles from source without any signing secrets (it produces an unsigned APK that can be signed downstream), which keeps the project friendly to F-Droid-style builds.

## Tech stack

| Area | Libraries |
|------|-----------|
| UI | Jetpack Compose, Material 3 |
| Dependency injection | Hilt |
| Networking | Retrofit, OkHttp, Gson |
| Images | Coil |
| Storage | DataStore, Room |
| Background work | WorkManager |
| Reader-mode extraction | readability4j, jsoup |

## Contributing

Issues and pull requests are welcome — please open an issue first for anything substantial. See [CONTRIBUTING.md](CONTRIBUTING.md), and [ROADMAP.md](ROADMAP.md) for planned work.

## License

Folio is free software, licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE).
