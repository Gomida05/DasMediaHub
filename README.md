# 🎬 DasMediaHub

**The Ultimate All-in-One Media Discovery & Download Suite**

DasMediaHub is a cutting-edge Android application engineered with **Jetpack Compose** and **Material 3**. It provides a unified, high-performance interface for discovering, streaming, and downloading content from across the web, including **YouTube, TikTok, and Instagram**.

> ⚠️ This project is intended for educational purposes. Users are responsible for complying with all applicable laws and platform terms.

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material_3-blue.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
---

## 🌟 Key Highlights

- **🚀 Performance-First Architecture:** Built using the latest Android standards (Compile/Target SDK 36, Kotlin 2.x).
- **🎞️ Premium Playback:** Seamless media experience powered by **AndroidX Media3 (ExoPlayer)** with background playback and PiP support.
- **📥 Smart Downloads:** A robust, background-resilient download manager with real-time status notifications.
- **🐍 Python-Powered Intelligence:** Leverages **Chaquopy** to execute Python scripts for advanced metadata extraction and content scraping.
- **☁️ Firebase Integration:** Real-time data persistence and analytics via Firestore.

---

## 🐍 Python Engine (`:python` module)

The heart of DasMediaHub's extraction logic lies in a dedicated Python module powered by **Chaquopy**. This allows the app to leverage powerful Python libraries for media scraping that are not natively available in Kotlin.

### 🛠 Core Python Libraries Used:
- **`yt-dlp`**: High-performance extraction of video/audio URLs and metadata from 1000+ sites.
- **`pytubefix`**: Specialized handling for YouTube-specific streams.
- **`youtube-search-python`**: Fast, lightweight YouTube searching without API keys.
- **`httpx` & `requests`**: Robust networking for scraping and API interaction.

> ⚠️ Some features rely on third-party tools such as `yt-dlp`. Their behavior and legality may vary depending on your region and usage.

---

## ✨ Features

### 🔍 Discovery & Search
- **Universal Search:** Find content across multiple platforms simultaneously.
- **Platform Specific Hubs:** Dedicated interfaces for YouTube, TikTok, and Instagram.
- **Rich Previews:** High-quality thumbnails and metadata for every result.

### 🎥 Media Experience
- **Advanced Player:** Support for HLS, DASH, and standard MP4/WebM formats.
- **Picture-in-Picture (PiP):** Continue watching while using other apps.
- **Background Audio:** Listen to your favorite content even when the screen is off.
- **History & Library:** Track what you've watched and manage your local downloads effortlessly.

### 🎨 Design & Personalization
- **Material 3 Interface:** Sleek, modern components with intuitive navigation.
- **Dynamic Color:** Adapts its theme based on your device's wallpaper.
- **Full Customization:** Toggle between Light and Dark modes with smooth transitions.

---

## 🛠 Tech Stack & Tools

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.x, Python (via Chaquopy) |
| **UI** | Jetpack Compose, Material 3, Navigation 3 |
| **Networking** | OkHttp 5, Kotlin Serialization |
| **Media** | AndroidX Media3 (ExoPlayer, Session), Coil, Glide |
| **Storage** | Firebase Firestore, Local File System |
| **Build** | Gradle Kotlin DSL, Version Catalog |

---

## 🚀 Getting Started

### 📋 Prerequisites
- **Android Studio Ladybug** (or later)
- **Android SDK 36**
- **JDK 17**
- **Local Python Interpreter** (for build-time compilation)

### 🔨 Installation & Build
1. Clone the repository:
   ```bash
   git clone https://github.com/Gomida05/DasMediaHub.git
   ```
2. Configure your `local.properties` (see below).
3. Open the project in Android Studio and sync with Gradle.
4. Run the app:
   ```bash
   ./gradlew installDebug
   ```

---

## 🔐 Local Configuration (`local.properties`)

To ensure all features work correctly, add the following to your `local.properties`:

```properties
# Path to your local Python interpreter (required by Chaquopy)
PYTHON_PATH=/usr/bin/python3

# App Signing (Required for Release)
KEYSTORE_FILE=C:/path/to/your/release.jks
KEYSTORE_PASSWORD=your_password
KEY_ALIAS=your_alias
KEY_PASSWORD=your_password
```

---

## 🤝 Contributing

Contributions make the open-source community an amazing place! If you'd like to contribute:
1. **Fork** the project.
2. **Create** your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`).
4. **Push** to the branch (`git push origin feature/AmazingFeature`).
5. **Open** a Pull Request.

---


## 📄 License

This project is licensed under the **Apache License 2.0**.

See the [LICENSE](LICENSE) file for full details.

---


## ⚠️ Legal Disclaimer

DasMediaHub is an open-source project provided for **educational and personal use only**.

This application does **not host, store, or distribute any media content**. It functions as a client that accesses publicly available data from third-party services.

### ❗ User Responsibility
By using this software, you agree that:
- You are solely responsible for your use of the application
- You will comply with all applicable local, national, and international laws
- You will adhere to the Terms of Service of all platforms accessed through this app

### 🚫 No Affiliation
DasMediaHub is **not affiliated with, endorsed by, or sponsored by**:
- YouTube (Google LLC)
- TikTok (ByteDance Ltd.)
- Instagram (Meta Platforms, Inc.)

All trademarks and copyrights belong to their respective owners.

### 📥 Content & Copyright
Accessing, downloading, or redistributing media may violate:
- Platform Terms of Service
- Copyright and intellectual property laws

You are solely responsible for ensuring that your actions are lawful and permitted.

### ⚠️ Limitation of Liability
The developers and contributors of DasMediaHub:
- Make **no guarantees** about the legality of usage in your jurisdiction
- Are **not responsible** for any misuse of the software
- Shall **not be held liable** for any claims, damages, or legal issues arising from its use

---

By using this software, you acknowledge and agree to this disclaimer.

**Developed with ❤️ by the DasMediaHub Team**
