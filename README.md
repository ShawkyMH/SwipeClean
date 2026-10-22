# ✨ SwipeClean

<div align="center">

### Modern, High-Performance Android Photo & Video Decluttering App
*Clean your camera roll effortlessly with fluid swipe gestures, Apple HIG-inspired design, and comprehensive bilingual support.*

[![Release](https://img.shields.io/badge/Release-v1.2.0-blueviolet?style=for-the-badge&logo=github)](https://github.com/ShawkyMH/SwipeClean/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--34)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Apple HIG](https://img.shields.io/badge/Design-Apple%20HIG%20Audited-000000?style=for-the-badge&logo=apple&logoColor=white)](https://developer.apple.com/design/human-interface-guidelines)
[![License](https://img.shields.io/badge/License-MIT-success?style=for-the-badge)](LICENSE)

</div>

---

## 📱 Overview

**SwipeClean** is a modern, responsive Android utility built from the ground up using **Kotlin**, **Jetpack Compose**, and **Material Design 3**. It makes organizing your overflowing photo and video gallery fast, safe, and intuitive through fluid swipe gestures audited against **Apple Human Interface Guidelines (HIG)** standards.

Unlike ordinary gallery cleaners that delete files immediately or require tedious checkboxes, SwipeClean introduces a safe **In-App Trash Bin** staging mechanism:
- 👈 **Swipe Left:** Send item to the in-app Trash Bin (staged for deletion, fully reversible at any time).
- 👉 **Swipe Right:** Keep the photo or video and advance to the next item.
- 🗑️ **Review & Confirm:** Inspect staged files in the Trash Bin, sort them by file size or date, restore with a single tap, or perform a permanent batch deletion with Android scoped storage safety.

---

## 🌟 Key Features

### 1. 🎴 Ergonomic Gestures & Apple HIG Navigation
- **Fluid Swipe Deck:** Interactive cards with velocity physics, dynamic tilt angle, and color-coded action badges (Green for Keep, Red for Trash).
- **Persistent Bottom Navigation Bar:** Clean one-handed switching between **Clean Deck**, **Gallery Grid**, and **Trash Bin**, with live badge counters.

### 2. 🎬 Advanced Video Player with Live Scrubbing
- **AndroidX Media3 ExoPlayer Engine:** Fast, hardware-accelerated playback for MP4, MKV, WebM, and 3GP formats.
- **Interactive Scrubber Slider:** Draggable progress slider for instant frame scrubbing and precise second-by-second navigation.
- **Audio Control & Lifecycle Safety:** Quick mute/unmute toggle, and automatic background pausing when minimizing the app or switching screens to eliminate audio overlap and battery drain.

### 3. 🔍 Fullscreen Inspection with Zoom & Pan
- **Ultra-HD Inspection:** Double-tap or pinch-to-zoom up to 220% on any image or video.
- **Configurable Fullscreen Behavior:** Choose between standard gallery flipping (Browse Mode) or active swiping (Clean Mode) while in fullscreen inspection.

### 4. 💾 Multi-Storage Drive Discovery
- Intelligently discovers and categorizes media items across:
  - 📱 **Internal Device Storage**
  - 💾 **External MicroSD Cards**
  - 🔌 **Removable USB OTG Flash Drives & External SSDs**
- Filter and sort by storage source with item counts per volume.

### 5. 🌐 100% Comprehensive Bilingual Localization
- Native, grammatically precise translations in both **English** and **Arabic**.
- Complete bidirectional layout support (**LTR** for English and **RTL** for Arabic), covering all buttons, menus, dialogs, badges, and system messages.

### 6. 🎨 Material 3 Theming & Accessibility (WCAG AA)
- Seamless support for **Dark Mode**, **Light Mode**, and **System Default**.
- Audited color contrast ratios conforming to **WCAG AA (≥ 4.5:1)** for optimal daylight and low-light legibility.

### 7. 🚀 Built-In GitHub Updates & One-Tap Installer
- Automated update detection on app startup, background checks, and system tray notifications.
- **In-App Streaming Download:** Live progress bar with completion percentage and downloaded megabytes.
- **Direct Package Installer:** Seamlessly launches Android's native installer via secure `FileProvider` upon completion.

### 8. 💬 Direct Developer Support via WhatsApp
- Send bug reports or feature requests directly to the developer with automated device metadata (device model, Android version, app build).

---

## 🛠️ Architecture & Tech Stack

```
SwipeClean/
├── app/
│   ├── src/main/java/com/swipeclean/app/
│   │   ├── data/            # MediaItem, Repository, AppStrings, Enums
│   │   ├── ui/
│   │   │   ├── components/  # SwipeCard, FullscreenDialog, FeedbackDialog, UpdateDialog
│   │   │   ├── screens/     # SwipeScreen, GalleryScreen, TrashScreen, SettingsScreen
│   │   │   └── theme/       # Color, Theme, Type (Material 3)
│   │   ├── viewmodel/       # MediaViewModel (Coroutines, StateFlow, MediaStore API)
│   │   ├── MainActivity.kt  # Compose Entry Point & Navigation Graph
│   │   └── AppConfig.kt     # App metadata and version constants
```

- **Architecture:** Unidirectional Data Flow (UDF) + Model-View-ViewModel (MVVM)
- **UI Framework:** Jetpack Compose (BOM 2024.02.00) + Material 3
- **Async & Reactive:** Kotlin Coroutines + `StateFlow` / `SharedFlow`
- **Image Pipeline:** Coil 2.5.0 + Video Frame Decoder
- **Media Engine:** AndroidX Media3 ExoPlayer 1.2.1
- **Storage Layer:** Android MediaStore ContentResolver API (Scoped Storage compliant)
- **Minimum SDK:** Android 8.0 (API Level 26)
- **Target SDK:** Android 14 (API Level 34)

---

## 📥 Download & Installation

You can download the latest signed release APK directly from GitHub:

👉 **[Download SwipeClean v1.2.0 APK](https://github.com/ShawkyMH/SwipeClean/releases/latest/download/SwipeClean.apk)**

### Installation Steps:
1. Download the `SwipeClean.apk` file on your Android device (or use the built-in in-app updater).
2. Open the file and enable **Allow from this source** if prompted by Android.
3. Grant photos and videos media access permissions when launched.

---

## 🏗️ Building from Source

### Prerequisites:
- JDK 17 or higher
- Android SDK 34 (API Level 34)
- Android Studio Iguana (2023.2.1) or newer / Gradle CLI

### Build Commands:
```bash
# Clone the repository
git clone https://github.com/ShawkyMH/SwipeClean.git
cd SwipeClean

# Build debug APK
cd SwipeClean
./gradlew assembleDebug

# Build signed release APK
./gradlew assembleRelease
```
The output APK will be located at:
`SwipeClean/app/build/outputs/apk/release/app-release.apk`

---

## 🔒 Permissions & Privacy

SwipeClean operates **100% locally on your device**. None of your photos, videos, or personal data are ever uploaded to any external server or third-party service.

| Permission | Purpose |
| :--- | :--- |
| `READ_MEDIA_IMAGES` | Access and display gallery photos on Android 13+ |
| `READ_MEDIA_VIDEO` | Access and play gallery videos on Android 13+ |
| `READ_EXTERNAL_STORAGE` | Backward-compatible access on Android 8.0 - 12 |
| `INTERNET` | Update checks, streaming APK downloads, and WhatsApp contact |
| `POST_NOTIFICATIONS` | Deliver update alerts on Android 13+ |
| `REQUEST_INSTALL_PACKAGES` | Directly launch Android Package Installer for downloaded updates |

---

## 👤 Author

- **ShawkyMH**
- **Email:** [royred222@gamil.com](mailto:royred222@gamil.com)
- **GitHub:** [@ShawkyMH](https://github.com/ShawkyMH)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
