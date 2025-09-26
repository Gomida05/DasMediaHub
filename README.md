# 🎬 DasMediaHub

**Transforming Media Experience with Limitless Innovation**

An extensible, multi-module Android media framework supporting background media playback, theme customization, content discovery, and smart download management.

![Last Commit](https://img.shields.io/github/last-commit/Gomida05/DasMediaHub)
![Top Language](https://img.shields.io/github/languages/top/Gomida05/DasMediaHub)
![Repo Languages](https://img.shields.io/github/languages/count/Gomida05/DasMediaHub)

---

## 📚 Table of Contents

- [🧾 Overview](#-overview)
- [🔍 Why DasMediaHub?](#-why-dasmediahub)
- [🚀 Getting Started](#-getting-started)
- [🛠 Installation](#-installation)

---

## 🧾 Overview

**DasMediaHub** is an all-in-one Android media platform. It enables seamless media consumption through:

- 🔊 Audio and video playback with background support
- 📥 File and app update download management
- 🔎 YouTube-powered content search and preview
- 🧱 Modular architecture supporting Kotlin, Compose, XML, and Python tools

---

## 🔍 Why DasMediaHub?

| Feature                        | Description                                                         |
|--------------------------------|---------------------------------------------------------------------|
| 🎨 Custom Themes & Light/Dark  | Fully themeable Compose UI with user personalization                |
| 🎧 Background Media Playback   | ExoPlayer & MediaSession support with PiP and controls              |
| 📥 Download Management         | Background-safe download manager with progress and notifications    |
| 🔍 Smart Discovery             | YouTube API integration, search history, and result previews        |
| 🛎️ Notification Integration   | Native Android notifications for media and download controls        |
| 🧱 Multi-Module Project        | Separation of concerns using Gradle + Kotlin DSL modules            |
| 🐍 Kotlin + Python Integration | Python for automation, metadata scraping, or future ML enhancements |

---

## 🚀 Getting Started

### 📦 Prerequisites

- Android Studio
- Kotlin 1.9+
- Gradle 8.0+
- Python 3.9+
- Git CLI

## 🛠 Installation

```bash
# Clone the repository
git clone https://github.com/Gomida05/DasMediaHub.git

# Move into the project directory
cd DasMediaHub

# Sync and build the project
./gradlew build
```

---

## 🔐 Required Local Configuration

Before building or releasing the app, you must create a `local.properties` file in the root directory of the project (same level as `build.gradle`). This file should contain the following entries:

```properties
# Path to your local Python interpreter
PYTHON_PATH=/usr/bin/python3

# Release keystore configuration (used for signing the app)
KEYSTORE_FILE=/absolute/path/to/your/release.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```
