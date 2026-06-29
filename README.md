# DasMediaHub

**The Ultimate All-in-One Media Discovery and Download Suite**

DasMediaHub is a high-performance Android application engineered with Jetpack Compose and Material 3. It provides a unified, polished interface for discovering, streaming, and downloading content from platforms like YouTube, TikTok, and Instagram.

> This project is intended for educational purposes. Users are responsible for complying with all applicable laws and platform terms.

[![App Version](https://img.shields.io/badge/Version-16.0-blue.svg)](https://github.com/Gomida05/DasMediaHub/releases)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.4.0-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material_3-blue.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Key Highlights

- **Modern Architecture:** Built using the latest Android standards (Target SDK 37, Kotlin 2.4.0, AGP 9.2.1).
- **Premium Playback:** Seamless media experience powered by AndroidX Media3 (ExoPlayer), featuring zero-flicker rotation and background playback.
- **Reliable Downloads:** A custom, background-resilient engine with persistent state tracking and resumable download support.
- **Python Integration:** Leverages Chaquopy to execute Python scripts for high-accuracy metadata extraction.
- **Unified Experience:** Clean, ad-free interface designed for speed and privacy.

---

## 🌟 What's New in v16.0

- **UI Overhaul:** Complete Material 3 refinement with smoother animations and enhanced card designs.
- **Performance Boost:** Updated to Kotlin 2.4.0 and AGP 9.2.1 for faster build times and runtime efficiency.
- **Reliability:** Improved download engine with better state persistence and error handling.
- **Refined UX:** Removed all legacy "Beta" components for a more stable, production-ready feel.
- **Enhanced Playback:** Latest Media3 integration for superior streaming quality and PiP support.

## Core Modules

### :app (The Hub)
The primary UI layer built with Navigation 3. It orchestrates media services and domain logic with a robust state management system to ensure a smooth user journey.

### :python (Extraction Engine)
The heart of DasMediaHub's extraction logic. Powered by Chaquopy 17.0.0, it utilizes:
- **yt-dlp**: Comprehensive metadata and stream extraction.
- **pytubefix**: Specialized support for YouTube content.
- **Advanced Scraping**: High-performance networking for reliable data retrieval.

### :downloader (Storage & Queue)
A decoupled module dedicated to the download lifecycle. It manages concurrent tasks, sequential queuing, and ensures all tasks survive app restarts or system kills.

---

## Features

### Discovery and Search
- **Universal Search:** Instant results across supported platforms.
- **Rich Metadata:** High-quality thumbnails, detailed descriptions, and channel information.
- **Deep Linking:** Automatic handling of external media links.

### Media & Playback
- **Advanced Player:** Support for HLS, DASH, and standard MP4/WebM with intelligent buffering.
- **Picture-in-Picture (PiP):** Full multitasking support with seamless state synchronization.
- **Background Playback:** Audio-only mode for listening on the go.
- **Library History:** Local tracking of watch history and saved items.

### Design & Privacy
- **Material 3 UI:** Polished components with fluid animations.
- **Dynamic Theming:** Adapts to your device's wallpaper and system theme.
- **Privacy-First:** All history and preferences are stored locally on your device.

---

## Tech Stack

| Category       | Technology                                           |
|:---------------|:-----------------------------------------------------|
| **Language**   | Kotlin 2.4.0, Python 3.14 (via Chaquopy 17.0.0)      |
| **UI**         | Jetpack Compose (BOM 2026.06.00), Material 3         |
| **Networking** | Ktor 3.5.1, OkHttp 5                                 |
| **Media**      | AndroidX Media3 1.10.1, Coil 2.7.0                   |
| **Database**   | Room 3.0.0-rc01, DataStore 1.2.1, Firebase Firestore |
| **Build Tools** | AGP 9.2.1, Gradle 9.x, KSP 2.3.6                     |

---

## Getting Started

### Prerequisites
- **Android Studio** (Latest Stable)
- **Android SDK 37**
- **JDK 21**

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Gomida05/DasMediaHub.git
   ```
2. Configure `local.properties`:
   ```properties
   PYTHON_PATH=/path/to/your/python3
   KEYSTORE_FILE=/path/to/your/release.jks
   KEYSTORE_PASSWORD=your_password
   KEY_ALIAS=your_alias
   KEY_PASSWORD=your_password
   ```
3. Sync with Gradle and run:
   ```bash
   ./gradlew installDebug
   ```

---

## License

This project is licensed under the **Apache License 2.0**.

---

## Legal Disclaimer

DasMediaHub is an open-source project for **educational and personal use only**. The application does **not host or distribute media content**; it acts as a client for publicly available data.

**Developed by the DasMediaHub Team**
