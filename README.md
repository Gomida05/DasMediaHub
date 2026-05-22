# DasMediaHub

**The Ultimate All-in-One Media Discovery and Download Suite**

DasMediaHub is a cutting-edge Android application engineered with Jetpack Compose and Material 3. It provides a unified, high-performance interface for discovering, streaming, and downloading content from across the web, including YouTube, TikTok, and Instagram.

> This project is intended for educational purposes. Users are responsible for complying with all applicable laws and platform terms.

[![App Version](https://img.shields.io/badge/Version-14.0-blue.svg)](https://github.com/Gomida05/DasMediaHub/releases)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.3.21-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material_3-blue.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Key Highlights

- **Performance-First Architecture:** Built using the latest Android standards (Target SDK 37, Kotlin 2.3.21).
- **Refined Playback Experience:** Seamless media switching powered by AndroidX Media3 (ExoPlayer). Optimized for smooth transitions, zero-flicker rotation, and robust background playback.
- **Resumable Downloads:** A custom, background-resilient download engine supporting HTTP Range requests for reliable content saving.
- **Python-Powered Intelligence:** Leverages Chaquopy to execute Python scripts for high-accuracy metadata extraction and advanced content scraping.
- **Real-time Data Persistence:** Integrated with Firebase Firestore for feedback collection and Room/DataStore for local history and favorites management.
- **Developer-Friendly Codebase:** Fully documented with comprehensive KDocs and usage examples across all core modules.

---

## Core Modules

### :app (The Interface)
The main UI layer built with Navigation 3. It coordinates between the media services and the domain layers, featuring a unified state management system for the video player to ensure a polished user experience.

### :python (The Extraction Engine)
The heart of DasMediaHub's extraction logic. Powered by Chaquopy, it leverages high-performance Python libraries:
- **yt-dlp**: Multi-site metadata and stream extraction.
- **pytubefix**: Specialized handling for YouTube-specific streams.
- **httpx & requests**: Robust networking for scraping and API interaction.

### :downloader (The Storage Layer)
A decoupled, clean-architecture module dedicated to managing the download lifecycle. It handles concurrent tasks, sequential queuing, and persistent state tracking to ensure downloads survive app restarts.

---

## Features

### Discovery and Search
- **Universal Search:** Find content across multiple platforms simultaneously.
- **Metadata Richness:** High-quality thumbnails, formatted view counts, and channel details for every result.
- **Deep Link Support:** Seamlessly handles external links from gomida05.com and YouTube.

### Media Experience
- **Advanced Player:** Support for HLS, DASH, and standard MP4/WebM formats with unified state handling.
- **Picture-in-Picture (PiP):** Multitasking support with automatic state synchronization.
- **Background Audio:** Listen to content while the screen is off or while using other apps.
- **Library Management:** Persistent Watch History and "Save for Later" functionality.

### Design and Personalization
- **Material 3 UI:** Modern components with intuitive navigation.
- **Dynamic Color:** The interface adapts its theme based on your device's wallpaper.
- **Custom Storage:** User-definable paths for Music and Video libraries.

---

## Tech Stack and Tools

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.3.21, Python 3.14 (via Chaquopy 17.0.0) |
| **UI** | Jetpack Compose, Material 3, Navigation 3 |
| **Networking** | Ktor 3.5.0, OkHttp 5 |
| **Media** | AndroidX Media3 (ExoPlayer, Session), Coil, Glide |
| **Storage** | Room DB 3.0.0-alpha05, DataStore, Firebase Firestore |
| **Build** | Gradle 9.x (Kotlin DSL), Version Catalog, KSP |

---

## Getting Started

### Prerequisites
- **Android Studio Ladybug** (or later)
- **Android SDK 37**
- **JDK 21**
- **Local Python Interpreter** (for build-time compilation)

### Installation and Build
1. Clone the repository:
   ```bash
   git clone https://github.com/Gomida05/DasMediaHub.git
   ```
2. Configure your `local.properties`:
   ```properties
   PYTHON_PATH=/path/to/your/python3
   KEYSTORE_FILE=C:/path/to/your/release.jks
   KEYSTORE_PASSWORD=your_password
   KEY_ALIAS=your_alias
   KEY_PASSWORD=your_password
   ```
3. Open the project in Android Studio and sync with Gradle.
4. Run the app:
   ```bash
   ./gradlew installDebug
   ```

---

## Contributing

Contributions make the open-source community an amazing place! If you'd like to contribute:
1. **Fork** the project.
2. **Create** your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`).
4. **Push** to the branch (`git push origin feature/AmazingFeature`).
5. **Open** a Pull Request.

---

## License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for full details.

---

## Legal Disclaimer

DasMediaHub is an open-source project provided for **educational and personal use only**. This application does **not host, store, or distribute any media content**. It functions as a client that accesses publicly available data from third-party services.

**Developed by the DasMediaHub Team**
