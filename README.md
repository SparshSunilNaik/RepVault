# RepVault

A premium, offline-first workout tracker for Android — built with Jetpack Compose, Material 3, and Room.

## Features

- **Log Workouts** — track sets, reps, and weight with a clean, fast interface
- **Workout History** — browse past sessions grouped by date with a GitHub-style calendar heatmap
- **Edit & Delete** — inline editing of sets/exercises, delete with confirmation dialogs
- **Auto-Fill Previous Sets** — quickly populate an exercise with your last session's data
- **Exercise Statistics** — per-exercise stats (best weight, total volume, sets, sessions, avg reps, last trained) with progress charts
- **Dashboard** — streak tracking, total volume, total sets, favorite exercise
- **Export/Import** — full JSON backup via system file picker
- **59 Pre-loaded Exercises** — ready to use out of the box
- **Dark Mode** — premium dark UI (#0A0A0A background, Material 3)

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3, Navigation Compose
- **Database:** Room (SQLite) with KSP
- **Architecture:** MVVM (ViewModel + Repository + DAO)
- **DataStore:** User preferences (first-launch name prompt)
- **Backup:** Gson for JSON export/import
- **Min SDK:** 26 | **Target SDK:** 36

## Building

```bash
git clone https://github.com/SparshSunilNaik/RepVault.git
cd RepVault
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.
