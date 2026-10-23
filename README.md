# Waqt (وقت) - Modern Islamic Prayer Times & Widget

<p align="center">
  <img src="app/src/main/res/drawable/ic_waqt_logo.png" width="120" height="120" alt="Waqt Logo" style="border-radius: 24px;" />
</p>

<p align="center">
  <b>A privacy-focused, aesthetically refined Islamic prayer times application and interactive Android home screen widget built with Jetpack Compose & Material 3.</b>
</p>

<p align="center">
  <a href="https://github.com/JasimIhsan/Waqt-Azan-time/releases"><img src="https://img.shields.io/github/v/release/JasimIhsan/Waqt-Azan-time?style=for-the-badge&color=2DD4BF" alt="Latest Release" /></a>
  <img src="https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge" alt="License" />
</p>

---

## 🌟 Key Features

### 📱 Interactive Home Screen Widget
- **Glance AppWidget**: Displays the next prayer, dynamic real-time countdown, and full daily prayer schedule directly on your home screen.
- **One-Tap Pinning**: Pin the widget directly from the app with Android 8.0+ widget request support.
- **Auto-Sync**: Automatically synchronizes with background prayer transitions and location updates.

### 🔔 10 Ambient Minimalist Notification Tones
- Replace harsh alarms with 10 custom, calming ambient chimes and Solfeggio frequencies:
  1. **Zen Chime** (528 Hz soothing Solfeggio chime)
  2. **Minimal Bell** (880 Hz crisp metallic bell)
  3. **Gentle Marimba** (Warm two-note wooden motif)
  4. **Soft Gong** (Low meditative warm chime)
  5. **Celestial Drop** (Pure water droplet sweep)
  6. **Serene Ting** (Clear high crystal triangle)
  7. **Peaceful Harp** (3-note ascending arpeggio)
  8. **Subtle Ripple** (Smooth harmonic chord)
  9. **Crystal Ping** (Crisp modern acoustic ping)
  10. **Ambient Glow** (Deep warm tonal decay)
- **Granular Timing**: Options for alert on exact Azan time, 30m before prayer ends, 15m before, and 5m urgent reminder.
- **Instant Audio Preview**: Audition each tone directly within the app before selecting.

### 🧭 Accurate Offline Calculation & GPS Tracking
- **100% Offline Math**: Calculates high-precision prayer times mathematically on-device without relying on external web APIs.
- **Adaptive Displacement Tracking**: Detects location changes in the background and updates prayer times accordingly.
- **Global Calculation Authorities**:
  - Muslim World League (MWL)
  - Egyptian General Authority of Survey
  - University of Islamic Sciences, Karachi
  - Umm Al-Qura University, Makkah
  - Dubai Islamic Affairs (UAE)
  - Moonsighting Committee Worldwide
  - ISNA (North America)
  - Kuwait Ministry of Awqaf
  - Qatar Ministry of Awqaf
  - MUIS (Singapore)
  - Other / Custom
- **Asr Juristic Methods**: Standard (Shafi'i, Maliki, Hanbali) and Hanafi (later shadow) methods.

### 🔄 In-App Auto-Updater via GitHub Releases
- Automatically checks for updates directly from GitHub Releases.
- Shows an update prompt on the Dashboard with release notes.
- In-app direct APK downloader with live progress tracking and seamless install trigger.

### 🎨 Modern Aesthetic Design
- Custom Dark Theme built around deep midnight slate (`#0B0F19`), rich Waqt Teal (`#2DD4BF`), and gentle sage accents (`#86EFAC`).
- Fluid animations, frosted card borders, and high legibility typography.

---

## 📲 Download & Installation

You can download the latest pre-compiled APK directly from the **[Releases Page](https://github.com/JasimIhsan/Waqt-Azan-time/releases)**.

1. Download `waqt-v1.0.0.apk`.
2. Tap the downloaded APK on your Android device to install.
3. Grant notification and location permissions for automatic local prayer calculation.

---

## 🛠️ Architecture & Tech Stack

```
com.waqt.app
├── data/
│   ├── calculator/       # Adhan prayer calculation engine (BatoulApps)
│   ├── location/         # GPS Location & displacement tracker
│   ├── notification/     # Audio notification player & AlarmManager scheduler
│   ├── receiver/         # Boot & prayer transition broadcast receivers
│   ├── repository/       # DataStore preference storage
│   └── updater/          # GitHub Releases updater & APK downloader
├── domain/
│   └── model/            # Domain entities (PrayerSchedule, Sounds, etc.)
└── ui/
    ├── dashboard/        # Main Dashboard screen & ViewModel
    ├── settings/         # Settings & Notification screens
    ├── updater/          # In-app update modal dialog
    ├── widget/           # Jetpack Glance Home Screen Widget
    └── theme/            # Color palettes, typography & theme system
```

- **Architecture**: MVVM with Clean Architecture principles
- **UI Toolkit**: Jetpack Compose & Material 3
- **Widgets**: AndroidX Glance AppWidget
- **Concurrency**: Kotlin Coroutines & StateFlow
- **Storage**: Jetpack DataStore (Preferences)
- **Calculation**: [BatoulApps Adhan Kotlin/Java library](https://github.com/batoulapps/adhan-java)
- **Language**: Kotlin 2.0+ (Targeting Android 15 / SDK 35, Min SDK 26)

---

## 💻 Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or command-line tools
- JDK 17
- Android SDK 35

### Steps
```bash
# Clone the repository
git clone https://github.com/JasimIhsan/Waqt-Azan-time.git
cd Waqt-Azan-time

# Build the debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install to connected device
./gradlew installDebug
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Developed with ❤️ by **[Jasim Ihsan M](https://github.com/JasimIhsan)**.
