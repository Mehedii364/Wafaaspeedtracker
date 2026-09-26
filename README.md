# Wafa Speed Tracker 🚗💨

**Live Speed. Smart Tracking. Every Journey.**

> Developed by: **Md. Mehedi Hasan**  
> Brand: **Mehedi364 · Wafa Zone**  
> Role: **Web Developer · Software Engineer · Programmer**  
> Website: [https://www.wafazone.site.je/](https://www.wafazone.site.je/)  
> Portfolio: [https://mehedii364.github.io/](https://mehedii364.github.io/)  
> GitHub: [https://github.com/Mehedi364](https://github.com/Mehedi364)  
> Email: `useable.me2@gmail.com`
>
> 
> DOWNLOAD LINK : https://github.com/Mehedii364/Wafaaspeedtracker/blob/main/APK_DOWNLOAD/app-debug.apk

---

## 🌟 Overview

**Wafa Speed Tracker** is a native Android GPS speedometer, route recorder, and telemetry application built with Kotlin, Jetpack Compose, Room database, and Google Play Services Location.

### 🛡️ Critical Tracking Rule: NO START = NO RECORDING

The application adheres strictly to the rule:
- **No automatic trip recording.**
- Prior to pressing **START TRIP**, real-time GPS telemetry may be displayed on the speedometer, but **NO** trips, distance, duration, route coordinates, or high-speed events are saved or accumulated.
- Strict state machine: `IDLE` ➔ `START` ➔ `TRACKING` ➔ `PAUSED` ➔ `RESUME` ➔ `STOP & SAVE` ➔ `IDLE`.

---

## 🚀 Key Features

1. **Precision Cockpit Speedometer**:
   - Digital HUD mode & high-contrast analog dial gauge.
   - Large central speed readout (km/h and mph switchable).
   - Moving vs. Idle badge indicator.
   - Full-screen landscape HUD mode for car dashboard mounting.

2. **Live Interactive Vector Map**:
   - Vehicle heading/bearing orientation marker.
   - Live gradient route polyline.
   - Accuracy halo & re-center follow-me controls.
   - Standard, Satellite, and Topographic grid layer options.

3. **High-Speed Monitoring & Warnings**:
   - Configurable speed limits (30, 40, 50, 60, 80, 100, 120, custom).
   - Haptic vibration and visual warning banner.
   - Automatic logging of speed exceedance events during active trips.

4. **Trip History & Route Replay**:
   - Full route summary with moving time, stopped time, average & maximum speeds.
   - Speed timeline profile graph.
   - Interactive Route Replay with slider, pause/play, and speed multipliers (1x, 2x, 5x).
   - Export routes to **GPX**, **CSV**, and **JSON**.

5. **Multi-Vehicle Garage**:
   - Profile management for Cars, Motorcycles, Trucks, and Buses.
   - Cumulative odometer and per-vehicle logs.

6. **Fuel & Maintenance Managers**:
   - Refill logging with volume, price per liter, total cost, and mileage.
   - Service schedules: Engine oil, tires, brakes, battery, and overhaul reminders.

7. **Wafa AI Monitor**:
   - Local on-device intelligence and optional private cloud proxy backend.
   - Natural language queries over your driving history (*"What was my longest trip?"*, *"Show my top speed"*).
   - Does **not** transmit raw continuous GPS points; only aggregated summary metrics.

---

## 🛠️ Build & APK/AAB Pipeline

### 1. Build Debug APK
```bash
gradle assembleDebug
```
The generated APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Unit & Robolectric Tests
```bash
gradle testDebugUnitTest
```

### 3. Build Release Bundle (AAB)
Configure your signing secrets in environment variables:
```bash
export KEYSTORE_PATH="/path/to/keystore.jks"
export STORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"
gradle bundleRelease
```
The generated AAB will be at:
`app/build/outputs/bundle/release/app-release.aab`

---

Developed by **Mehedi364** ❤️
