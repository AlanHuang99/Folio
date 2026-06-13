# Folio roadmap

Folio is a clean Android client for a self-hosted FreshRSS server over the Google
Reader API. It is built in phases; each phase is a usable increment.

## Phase 0 — Identity & scaffold ✅
Project skeleton, Charcoal icon + theme, build/release/CI, F-Droid reproducibility
settings, fastlane metadata, GPL-3.0. Reproducible-from-day-one (to be verified on
F-Droid CI before submission).

## Phase 1 — Login
Server URL + username + API password → Google Reader ClientLogin → store the token
securely (DataStore). Sign-out.

## Phase 2 — Subscriptions & navigation
Fetch feeds and categories. Navigate across All / Unread / Starred / by Category /
by Feed, with a single Scaffold and bottom navigation.

## Phase 3 — Article list
Stream contents: title, feed, time, excerpt, thumbnail, read/unread. Mark read/unread,
star, and pull-to-refresh.

## Phase 4 — Reader view
Rendered article (HTML → Compose, images via Coil), star, mark-read, share,
open-in-browser, and swipe to next/previous.

## Phase 5 — Sync & offline
Edit-tag read/star sync, background sync (WorkManager), local cache, unread badges.

## Phase 6 — Polish & ship
Search, settings, and a selectable **Appearance** — pick the launcher icon and the
matching in-app theme from a set of looks (Charcoal, Evergreen, Slate, Paper, Ocean,
Plum, and the Dog-ear / Line-art / Wide-fan styles). F-Droid reproducibility
verification, screenshots, release, and the fdroiddata submission.
