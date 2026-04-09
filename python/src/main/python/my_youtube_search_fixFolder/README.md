# 🛠️ My YouTube Search Fix

A patched and optimized version of `youtube-search-python` specifically tailored for use within the **DasMediaHub** Android application.

## 📌 Purpose

This library provides a high-performance, API-key-free way to search for YouTube content. It has been modified to:
- **Fix Compatibility Issues:** Resolved specific extraction errors encountered in the original library.
- **Optimize for Mobile:** Refined networking and parsing logic to work seamlessly with Chaquopy on Android.
- **Support Advanced Extraction:** Enhanced support for playlists, channels, and stream URL fetching.

## 🚀 Key Features

- **Search:** Videos, Playlists, Channels, and more.
- **Suggestions:** Real-time search suggestions.
- **Metadata Extraction:** Detailed info including views, duration, and high-res thumbnails.
- **Transcript Support:** Fetching video transcripts when available.
- **Stream URL Fetching:** Integrated logic for retrieving direct media URLs.

## 🛠 Integration in DasMediaHub

This package is included as a local dependency in the `:python` module. It is automatically initialized alongside the Python engine in `MainApplication.kt` and utilized by `main.py` for all YouTube search queries.

---

*Part of the [DasMediaHub](https://github.com/Gomida05/DasMediaHub) project.*
