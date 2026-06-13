# Contributing to Folio

Thanks for your interest in Folio.

## Building

Folio is a standard Gradle Android project. With JDK 17 and the Android SDK installed:

```bash
./gradlew assembleDebug
./gradlew installDebug   # to a connected device
./gradlew lintDebug
```

## Guidelines

- Keep the project free of proprietary dependencies (no Play Services, Firebase, or
  analytics) so it remains buildable from source for F-Droid.
- Match the existing code style (Kotlin, Jetpack Compose, MVVM).
- Discuss larger changes in an issue before opening a pull request.
