# Engineering Specification & Scaffolding Prompt: Next Azan Native Android App & Glance Widget

You are an expert Senior Android Engineer. Build a production-ready, clean-architecture native Android application written in 100% Kotlin using Jetpack Compose (UI) and Jetpack Glance (AppWidget). 

The application has two primary surfaces:
1. An in-app dashboard displaying full daily prayer schedules with real-time GPS sync, reverse geocoding, and manual refresh.
2. A home-screen widget displaying the single "Next Azan" that auto-updates on exact prayer transitions and recalculates when the user's location shifts significantly.

---

## 1. Tech Stack & Dependencies

- **Language:** Kotlin
- **Min SDK:** 26 (Android 8.0) | **Target/Compile SDK:** 35
- **UI:** Jetpack Compose (Material3)
- **AppWidget:** Jetpack Glance (`androidx.glance:glance-appwidget:1.1.0`, `androidx.glance:glance-material3:1.1.0`)
- **Prayer Engine:** Batoul Apps `adhan-java` (`com.batoulapps.adhan:adhan:1.2.1`) for offline mathematical solar calculation
- **Location & Geocoding:** Google Play Services Location (`com.google.android.gms:play-services-location:21.3.0`) + Android native `Geocoder`
- **Asynchrony & State:** Kotlin Coroutines, StateFlow, AndroidX DataStore Preferences
- **Precise Timing & Background:** `AlarmManager` for exact prayer changeovers + `PendingIntent` with `FusedLocationProviderClient` for battery-efficient background displacement tracking (5 km threshold).

---

## 2. Architecture & File Structure

Implement clean separation of concerns:

```
app/src/main/java/com/example/azanapp/
├── data/
│   ├── calculator/
│   │   └── PrayerCalculator.kt         // Adhan wrapper (CalculationMethod, Madhab, next prayer logic)
│   ├── location/
│   │   ├── LocationHelper.kt           // FusedLocationProviderClient + Geocoder wrapper
│   │   └── LocationUpdateReceiver.kt   // BroadcastReceiver for displacement PendingIntent
│   ├── receiver/
│   │   └── PrayerAlarmReceiver.kt      // BroadcastReceiver for AlarmManager exact wakeups
│   └── repository/
│       └── UserPreferencesRepository.kt// DataStore for lat, lng, localityName, calculation params
├── domain/
│   └── model/
│       ├── PrayerSchedule.kt          // Daily 5 prayers + sunrise mapping
│       └── NextPrayerInfo.kt          // Name, Date, formatted time string, isTomorrow flag
├── ui/
│   ├── dashboard/
│   │   ├── DashboardScreen.kt         // Compose UI: Hero card for Next Azan, List of all prayers, TopBar sync
│   │   └── DashboardViewModel.kt      // Manages UI state, location permission flows, manual refresh
│   ├── theme/
│   │   ├── Color.kt                   // Modern slate/midnight palette (#0F172A, #1E293B, #38BDF8)
│   │   └── Theme.kt                   // Material3 theme definition
│   └── widget/
│       ├── NextAzanWidget.kt          // Jetpack Glance AppWidget definition
│       ├── NextAzanWidgetReceiver.kt  // GlanceAppWidgetReceiver
│       └── WidgetSyncManager.kt       // Coordinates Glance updates & AlarmManager scheduling
└── MainActivity.kt
```

---

## 3. Concrete Implementation Requirements

### A. Core Mathematical Engine (`PrayerCalculator.kt`)
- Rely on `com.batoulapps.adhan.*`.
- Configurable calculation method defaults to `CalculationMethod.MUSLIM_WORLD_LEAGUE` and Shafi'i Madhab.
- Implement:
  ```kotlin
  fun getNextPrayer(lat: Double, lng: Double, date: Date = Date()): NextPrayerInfo
  fun getDailySchedule(lat: Double, lng: Double, date: Date = Date()): Map<Prayer, Date>
  ```
- **Edge-case handling:** If all prayers for today are concluded (or `nextPrayer()` returns `NONE`/null), compute tomorrow's Fajr, tag `isTomorrow = true`, and return that timestamp.

### B. In-App Dashboard Screen (`DashboardScreen.kt`)
- Handle Android runtime permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).
- Display:
  - Top AppBar: Title, current reverse-geocoded City/Locality name, and an explicit GPS Sync IconButton.
  - Pull-to-refresh or tap-to-sync triggers immediate GPS location fetch and re-computes state.
  - Next Azan Hero Card: Bold upcoming prayer name, remaining countdown or formatted scheduled time (e.g., `04:32 PM`).
  - Daily Schedule List: Clean cards for Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha. Distinctly highlight the upcoming prayer item.

### C. Home-Screen Widget via Jetpack Glance (`NextAzanWidget.kt`)
- Compact, glanceable composable:
  - Header tag: "NEXT AZAN"
  - Prayer Name (e.g., "Asr", "Maghrib")
  - Scheduled Time (e.g., "04:32 PM")
  - Locality / City caption.
- Set `updatePeriodMillis="0"` in `res/xml/next_azan_widget_info.xml` to prevent arbitrary battery drain; rely strictly on event-based updates.

### D. Real-Time Location & Precise Alarm Synchronization
1. **Displacement Tracking:** Configure `FusedLocationProviderClient.requestLocationUpdates` via a `PendingIntent` targeting `LocationUpdateReceiver`:
   - `Priority.PRIORITY_BALANCED_POWER_ACCURACY`
   - `setMinUpdateDistanceMeters(5000f)` (wake up only after 5 km displacement).
   - On trigger: Persist coordinates to DataStore, recalculate the next Azan, refresh the Glance widget via `NextAzanWidget().updateAll(context)`, and arm the new transition alarm.
2. **Prayer Transition Alarms:**
   - Schedule exact alarms via `AlarmManager.setExactAndAllowWhileIdle()` to target `PrayerAlarmReceiver`.
   - On alarm fire: Redraw the Glance widget, calculate the next prayer time, and schedule the next alarm in sequence.

---

## 4. Deliverables Needed

Provide the complete, unabridged code for:
1. `app/build.gradle.kts` (with Compose and Glance dependencies configured).
2. `AndroidManifest.xml` (permissions, receivers, widget provider metadata).
3. `res/xml/next_azan_widget_info.xml`.
4. `PrayerCalculator.kt`.
5. `NextAzanWidget.kt` and `NextAzanWidgetReceiver.kt`.
6. `LocationUpdateReceiver.kt`, `PrayerAlarmReceiver.kt`, and `WidgetSyncManager.kt`.
7. `DashboardViewModel.kt` and `DashboardScreen.kt`.