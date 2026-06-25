# OmniTrack - All-in-One Activity & Habit Tracker

OmniTrack is a modern, feature-rich Android application designed for highly customizable logging of habits, daily chores, fitness activities, and personal events. Crafted with Jetpack Compose and Material Design 3, the app delivers a polished, cohesive, and seamless user experience.

---

## ✨ Features

- **Personalized Activity Logging**: Create, edit, and categorize multiple trackers (e.g., Steps, Hydration, Sleep, Habits, Tasks).
- **Professional Polish Theme**:
  - Balanced typography with beautiful display scales.
  - Modern card layouts utilizing generous negative space, sleek borders, and Material 3 design accents.
  - High-fidelity visual components (progress tracking rings, detail cards).
- **Dynamic Light & Dark Theme**:
  - Dedicated theme controller in the top bar allows toggling between **Light Mode**, **Dark Mode**, and **System Default**.
  - Consistent contrast ratio and eye-friendly color pairings in both light and dark states.
- **Local Persistence & Streaks**:
  - Integrated local **Room Database** for high performance and offline-first capabilities.
  - Tracks and calculates historical streaks, daily completion rates, and details.
- **Robust Verification Suite**:
  - Unit & Integration testing powered by **Robolectric**.
  - High-fidelity visual regression screenshots validated using **Roborazzi**.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture Repository pattern
- **Database**: Room Persistence Library
- **Asynchronous Flow**: Kotlin Coroutines & StateFlow
- **Testing**: JUnit, Robolectric, Roborazzi (Screenshot Testing)

---

## 🚀 Running the App

1. Import the project into **Android Studio** or compile via the terminal.
2. Run standard local unit tests with:
   ```bash
   gradle :app:testDebugUnitTest
   ```
3. To record reference Roborazzi screenshots:
   ```bash
   gradle :app:recordRoborazziDebug
   ```
