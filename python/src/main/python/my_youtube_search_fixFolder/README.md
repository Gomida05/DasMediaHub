# 🛠️ YouTube Search Core (DasMediaHub Edition)

A specialized, high-performance patch of `youtube-search-python` optimized for the **DasMediaHub** v16.0 release.

## 📌 Overview

This core module provides a robust, API-key-free bridge for searching YouTube content directly within the Android environment. It has been meticulously refined to ensure maximum compatibility with **Chaquopy** and mobile networking constraints.

### 🔧 Improvements in v16.0
- **Extraction Stability:** Fixed critical parsing errors caused by recent YouTube layout changes.
- **Mobile Optimization:** Drastically reduced memory footprint during large search result parsing.
- **Enhanced Reliability:** Improved timeout handling and retry logic for unstable mobile data connections.
- **Clean Architecture:** Refined internal logic to work seamlessly with the `:app` module's new state management.

## 🚀 Key Capabilities

- **Deep Search:** Instantly fetch Videos, Playlists, and Channel data.
- **Smart Suggestions:** Real-time search predictions as the user types.
- **Comprehensive Metadata:** Accurate view counts, durations, and high-resolution thumbnail extraction.
- **Media Stream Logic:** Integrated handlers for direct stream URL retrieval and transcript fetching.

## 🛠 Integration

This package serves as a local dependency for the `:python` module. It is initialized during the application startup and is the primary engine behind every YouTube query made in **MediaHub**.

---

*Part of the [DasMediaHub](https://github.com/Gomida05/DasMediaHub) project ecosystem.*
